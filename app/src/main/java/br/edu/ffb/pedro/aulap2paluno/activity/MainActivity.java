package br.edu.ffb.pedro.aulap2paluno.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import br.edu.ffb.pedro.aulap2paluno.AulaP2PAlunoApp;
import br.edu.ffb.pedro.aulap2paluno.R;
import br.edu.ffb.pedro.aulap2paluno.Utils;
import br.edu.ffb.pedro.aulap2paluno.callback.OnKillApp;
import br.edu.ffb.pedro.aulap2paluno.callback.OnStudentDialogClickOk;
import br.edu.ffb.pedro.aulap2paluno.event.MessageEvent;
import br.edu.ffb.pedro.aulap2paluno.model.DaoSession;
import br.edu.ffb.pedro.aulap2paluno.model.Questionnaire;
import br.edu.ffb.pedro.aulap2paluno.model.Quiz;
import br.edu.ffb.pedro.aulap2paluno.model.QuizData;
import br.edu.ffb.pedro.bullyelectionp2p.BullyElectionP2p;
import br.edu.ffb.pedro.bullyelectionp2p.event.BullyElectionEvent;
import br.edu.ffb.pedro.bullyelectionp2p.event.ClientEvent;
import br.edu.ffb.pedro.bullyelectionp2p.event.DataTransferEvent;
import br.edu.ffb.pedro.bullyelectionp2p.event.WifiP2pEvent;
import br.edu.ffb.pedro.bullyelectionp2p.payload.Payload;

import static br.edu.ffb.pedro.aulap2paluno.activity.LoginActivity.STUDENT_NAME;
import static br.edu.ffb.pedro.aulap2paluno.activity.LoginActivity.STUDENT_PREFERENCES;

public class MainActivity extends AppCompatActivity {

    private AulaP2PAlunoApp app;
    private BullyElectionP2p bullyElectionP2p;
    private LinearLayout quizContainer;
    private Quiz quiz;
    private ProgressDialog progressDialog;
    private boolean isSentQuestionnarieEvent;
    private boolean isLogoutEvent;
    private boolean aDataTransferFailureEventOccurred;
    private Questionnaire questionnaire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        app = (AulaP2PAlunoApp) getApplication();
        bullyElectionP2p = app.getBullyElectionP2p();

        setActionBarLeaderIcon();

        quizContainer = (LinearLayout) findViewById(R.id.quiz_container);
        quiz = new Quiz(this);

        FloatingActionButton fabSendQuestions = (FloatingActionButton) findViewById(R.id.fabSendQuestions);
        fabSendQuestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Enviar Questionário?")
                        .setMessage("Sua sessão será finalizada")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();
                                questionnaire = getAppDaoSession()
                                        .getQuestionnaireDao()
                                        .load(bullyElectionP2p.thisDevice.id);

                                if (questionnaire != null) {
                                    isSentQuestionnarieEvent = true;
                                    progressDialog.setTitle("Enviando...");
                                    progressDialog.setMessage("Enviando o questionário");
                                    progressDialog.show();

                                    questionnaire = quiz.getQuizResponse(quizContainer, questionnaire);

                                    if (bullyElectionP2p.thisDevice.isLeader) {
                                        questionnaire = quiz.getQuizResult(questionnaire);
                                    }

                                    questionnaire.setStudentName(getStudentName());

                                    QuizData quizData = new QuizData();
                                    quizData.message = QuizData.RESPONSE_QUIZ;
                                    quizData.questionnaire = questionnaire;
                                    bullyElectionP2p.sendToHost(quizData);
                                } else {
                                    Toast.makeText(MainActivity.this, "Por favor, aguarde um " +
                                                    "questionário ser enviado do professor",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Não", null)
                        .create();
                alertDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Deseja realmente sair?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                if (bullyElectionP2p.thisDevice.isRegistered) {
                                    isLogoutEvent = true;
                                    progressDialog.setTitle("Finalizando sessão...");
                                    progressDialog.setMessage("Finalizando");
                                    progressDialog.show();
                                    bullyElectionP2p.unregisterClient(false);
                                }
                            }
                        })
                        .setNegativeButton("Não", null)
                        .create();
                alertDialog.show();
                return true;
            case R.id.change_student_name:
                String studentName = getStudentName();
                Utils.showStudentChangeNameDialog(MainActivity.this, studentName, "Finalizada", new OnStudentDialogClickOk() {
                    @Override
                    public void onClick(AlertDialog dialog, String studentName) {
                        SharedPreferences.Editor editor = getApplicationContext()
                                .getSharedPreferences(STUDENT_PREFERENCES, MODE_PRIVATE).edit();
                        editor.putString(STUDENT_NAME, studentName);
                        editor.apply();

                        dialog.dismiss();
                        if (bullyElectionP2p.thisDevice.isRegistered) {
                            isLogoutEvent = true;
                            progressDialog.setTitle("Finalizando sessão...");
                            progressDialog.setMessage("Finalizando");
                            progressDialog.show();
                            bullyElectionP2p.unregisterClient(false);
                        }
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onClientEvent(ClientEvent clientEvent) {
        switch (clientEvent.event) {
            case ClientEvent.UNREGISTERED:
                Toast.makeText(MainActivity.this, "Dispositivo removido do professor",
                        Toast.LENGTH_SHORT).show();
                removeAllQuestionnaires();
                setResult(RESULT_OK);
                bullyElectionP2p.unregisterEventBus();
                Utils.finishApp(MainActivity.this, new OnKillApp() {
                    @Override
                    public void call() {
                        EventBus.getDefault().post(new MessageEvent(MessageEvent.EXIT_APP));
                        Log.d(BullyElectionP2p.TAG, "ENCERRANDO O APP: UNREGISTERED");
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                });
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataTransferEvent(DataTransferEvent dataTransferEvent) {
        String data = dataTransferEvent.data;
        switch (dataTransferEvent.event) {
            case DataTransferEvent.DATA_RECEIVED:
                try {
                    Payload payload = LoganSquare.parse(data, Payload.class);
                    switch (payload.type) {
                        case QuizData.TYPE:
                            QuizData quizData = LoganSquare.parse(data, QuizData.class);
                            switch (quizData.message) {
                                case QuizData.LOAD_QUIZ:
                                    quizData.questionnaire.setId(bullyElectionP2p.thisDevice.id);
                                    quiz.addQuiz(quizContainer, quizData.questionnaire);
                                    if (bullyElectionP2p.thisDevice.isLeader) {
                                        bullyElectionP2p.sendToAllDevices(quizData);
                                    }
                                    break;
                                case QuizData.RESPONSE_QUIZ:
                                    if (bullyElectionP2p.thisDevice.isLeader) {
                                        // O Líder corrige as questões dos alunos e as envia para o professor
                                        Log.d(BullyElectionP2p.TAG, "O Líder " +
                                                bullyElectionP2p.thisDevice.readableName +
                                                " recebeu um questionário");

                                        questionnaire = quizData.questionnaire;
                                        questionnaire = quiz.getQuizResult(questionnaire);

                                        quizData.message = QuizData.RESPONSE_QUIZ;
                                        quizData.questionnaire = questionnaire;
                                        bullyElectionP2p.sendToHost(quizData);
                                    }
                                    break;
                            }
                            break;
                    }
                } catch (IOException e) {
                    Log.e(BullyElectionP2p.TAG, "Falha ao serializar o arquivo JSON", e);
                }
                break;
            case DataTransferEvent.SENT:
                Log.d(BullyElectionP2p.TAG, "Dados enviados com sucesso");
                if (bullyElectionP2p.thisDevice.isRegistered && isSentQuestionnarieEvent) {
                    isLogoutEvent = true;
                    progressDialog.setTitle("Questionário enviado, Desconectando...");
                    progressDialog.setMessage("Finalizando sessão");
                    bullyElectionP2p.unregisterClient(false);
                }
                break;
            case DataTransferEvent.FAILURE:
                Log.e(BullyElectionP2p.TAG, "Falha ao enviar os dados");
                isSentQuestionnarieEvent = false;
                aDataTransferFailureEventOccurred = true;
                progressDialog.setTitle("Falha no envio");
                progressDialog.setMessage("Iniciando um nova eleição");
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWifiP2pEvent(WifiP2pEvent wifiP2pEvent) {
        switch (wifiP2pEvent.event) {
            case WifiP2pEvent.DISCONNECTED_FROM_ANOTHER_DEVICE:
                if (!isLogoutEvent) {
                    // isLogoutEvent fica true, pois esse evento reinicia a Wifi e quando a Wifi é
                    // desabilitada, o evento DISCONNECTED_FROM_ANOTHER_DEVICE é chamado, causando um loop
                    isLogoutEvent = true;
                    Toast.makeText(MainActivity.this, "O professor desconectou",
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    removeAllQuestionnaires();
                    bullyElectionP2p.unregisterEventBus();
                    progressDialog.setTitle("Finalizando sessão...");
                    progressDialog.setMessage("Finalizando");
                    progressDialog.show();
                    Utils.finishApp(MainActivity.this, new OnKillApp() {
                        @Override
                        public void call() {
                            Log.d(BullyElectionP2p.TAG, "ENCERRANDO O APP: DISCONNECTED_FROM_ANOTHER_DEVICE");
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        }
                    });
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBullyElectionEvent(BullyElectionEvent bullyElectionEvent) {
        switch (bullyElectionEvent.event) {
            case BullyElectionEvent.ELECTED_LEADER:
                Log.d(BullyElectionP2p.TAG, "Um novo líder foi eleito: "
                        + bullyElectionEvent.device.readableName);

                setActionBarLeaderIcon();

                if (aDataTransferFailureEventOccurred) {
                    aDataTransferFailureEventOccurred = false;
                    isSentQuestionnarieEvent = true;
                    Log.d(BullyElectionP2p.TAG, "Devido ao evento de falha no envio dos dados, " +
                            "o app tentará enviar novamente");

                    if (bullyElectionP2p.thisDevice.isLeader) {
                        questionnaire = quiz.getQuizResult(questionnaire);
                    }
                    QuizData quizData = new QuizData();
                    quizData.message = QuizData.RESPONSE_QUIZ;
                    quizData.questionnaire = questionnaire;
                    bullyElectionP2p.sendToHost(quizData);
                }
                break;
        }
    }

    public String getStudentName() {
        return getApplicationContext()
                .getSharedPreferences(STUDENT_PREFERENCES, MODE_PRIVATE).getString(STUDENT_NAME, "");
    }

    private void setActionBarLeaderIcon() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (bullyElectionP2p.thisDevice.isLeader) {
                actionBar.setIcon(R.drawable.ic_sheriff_enabled);
            } else {
                actionBar.setIcon(R.drawable.ic_sheriff_disabled);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(MainActivity.this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(MainActivity.this);
        super.onStop();
    }

    private void removeAllQuestionnaires() {
        // Remove todas as questões
        getAppDaoSession().getQuestionDao().deleteAll();
        // Remove o questionário padrão
        getAppDaoSession().getQuestionnaireDao().deleteAll();

        getAppDaoSession().clear();
    }

    private DaoSession getAppDaoSession() {
        return app.getDaoSession();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bullyElectionP2p.unregisterClient(false);
    }
}
