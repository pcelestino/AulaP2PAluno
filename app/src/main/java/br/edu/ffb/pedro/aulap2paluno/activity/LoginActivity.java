package br.edu.ffb.pedro.aulap2paluno.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import br.edu.ffb.pedro.aulap2paluno.AulaP2PAlunoApp;
import br.edu.ffb.pedro.aulap2paluno.R;
import br.edu.ffb.pedro.aulap2paluno.Utils;
import br.edu.ffb.pedro.aulap2paluno.adapter.ProfessorsListAdapter;
import br.edu.ffb.pedro.aulap2paluno.callback.OnKillApp;
import br.edu.ffb.pedro.aulap2paluno.callback.OnStudentDialogClickOk;
import br.edu.ffb.pedro.aulap2paluno.event.MessageEvent;
import br.edu.ffb.pedro.aulap2paluno.listener.OnProfessorsListItemClickListener;
import br.edu.ffb.pedro.bullyelectionp2p.BullyElectionP2p;
import br.edu.ffb.pedro.bullyelectionp2p.BullyElectionP2pDevice;
import br.edu.ffb.pedro.bullyelectionp2p.event.ClientEvent;
import br.edu.ffb.pedro.bullyelectionp2p.event.DataTransferEvent;
import br.edu.ffb.pedro.bullyelectionp2p.event.WifiP2pEvent;

public class LoginActivity extends AppCompatActivity implements OnProfessorsListItemClickListener {

    static final String STUDENT_PREFERENCES = "STUDENT_PREFERENCES";
    static final String STUDENT_NAME = "STUDENT_NAME";
    static final int RESTART_ACTIVITY_REQUEST = 1;

    private Toolbar toolbar;
    private String studentInputName = "";

    private AulaP2PAlunoApp app;
    private BullyElectionP2p bullyElectionP2p;

    private RecyclerView professorsList;
    private View mEmptyView;
    private ProfessorsListAdapter professorsListAdapter;

    private ProgressDialog connectingDialog;
    private ProgressDialog logoutDialog;

    private boolean isLogoutEvent;
    private boolean isRestartAppEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        app = (AulaP2PAlunoApp) getApplication();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.GONE);
        professorsList = (RecyclerView) findViewById(R.id.professorsList);
        mEmptyView = findViewById(R.id.emptyView);

        studentInputName = getStudentPreferences().getString(STUDENT_NAME, "");
        if (studentInputName.isEmpty()) {
            Utils.showStudentInputNameDialog(LoginActivity.this, new OnStudentDialogClickOk() {
                @Override
                public void onClick(AlertDialog dialog, String studentName) {
                    studentInputName = studentName;
                    SharedPreferences.Editor editor = getStudentPreferences().edit();
                    editor.putString(STUDENT_NAME, studentInputName);
                    editor.apply();

                    if (studentInputName.isEmpty()) {
                        Toast.makeText(LoginActivity.this, R.string.please_insert_your_name,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        toolbar.setVisibility(View.VISIBLE);
                        setupEasyP2P();
                        dialog.dismiss();
                    }
                }
            });
        } else {
            toolbar.setVisibility(View.VISIBLE);
            setupEasyP2P();
        }
    }

    public SharedPreferences getStudentPreferences() {
        return getApplicationContext().getSharedPreferences(STUDENT_PREFERENCES, MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                isRestartAppEvent = true;
                logoutDialog = ProgressDialog
                        .show(LoginActivity.this, "Reiniciando sessão...",
                                "Reiniciando");
                bullyElectionP2p.unregisterClient(false);
                return true;
            case R.id.logout:
                AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Deseja realmente sair?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                isLogoutEvent = true;
                                logoutDialog = ProgressDialog
                                        .show(LoginActivity.this, "Finalizando sessão...",
                                                "Finalizando");
                                bullyElectionP2p.unregisterClient(false);
                            }
                        })
                        .setNegativeButton("Não", null)
                        .create();
                alertDialog.show();
                return true;
            case R.id.change_student_name:

                Utils.showStudentChangeNameDialog(LoginActivity.this, studentInputName, "Reiniciada", new OnStudentDialogClickOk() {
                    @Override
                    public void onClick(AlertDialog dialog, String studentName) {
                        studentInputName = studentName;
                        SharedPreferences.Editor editor = getStudentPreferences().edit();
                        editor.putString(STUDENT_NAME, studentInputName);
                        editor.apply();
                        dialog.hide();

                        isRestartAppEvent = true;
                        logoutDialog = ProgressDialog
                                .show(LoginActivity.this, "Reiniciando sessão...",
                                        "Reiniciando");
                        bullyElectionP2p.unregisterClient(false);
                    }
                });

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void setupEasyP2P() {
        bullyElectionP2p = app.initBullyElectionP2p(studentInputName);
        bullyElectionP2p.discoverNetworkServices();
        setupProfessorsList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWifiP2pEvent(WifiP2pEvent wifiP2pEvent) {
        switch (wifiP2pEvent.event) {
            case WifiP2pEvent.SERVER_DEVICE_FOUND:
                professorsListAdapter.notifyDataSetChanged();
                break;
            case WifiP2pEvent.CONNECTED_TO_ANOTHER_DEVICE:
                goMainActivity();
                break;
            case WifiP2pEvent.DISCONNECTED_FROM_ANOTHER_DEVICE:
                // Tentou se conectar com o professor porém não houve resposta
                if (connectingDialog != null && connectingDialog.isShowing()) {
                    Toast.makeText(LoginActivity.this, "Não foi possível se conectar ao professor, " +
                            "por favor, tente novamente", Toast.LENGTH_LONG).show();
                    Utils.finishApp(LoginActivity.this, new OnKillApp() {
                        @Override
                        public void call() {
                            connectingDialog.dismiss();
                        }
                    });
                }
                break;
            case WifiP2pEvent.ERROR:
                if (connectingDialog != null && connectingDialog.isShowing()) {
                    connectingDialog.dismiss();
                }
                logoutDialog = ProgressDialog
                        .show(LoginActivity.this, "Erro de conexão...",
                                "O aplicativo será reiniciado");
                Utils.finishApp(LoginActivity.this, new OnKillApp() {
                    @Override
                    public void call() {
                        logoutDialog.dismiss();
                    }
                });
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onClientEvent(ClientEvent clientEvent) {
        switch (clientEvent.event) {
            case ClientEvent.REGISTERED:
                Log.d(BullyElectionP2p.TAG, "Dispositivo registrado com o servidor");
                break;
            case ClientEvent.UNREGISTERED:
                if (isLogoutEvent) {
                    Log.d(BullyElectionP2p.TAG, "Dispositivo removido do servidor");
                    Utils.finishApp(LoginActivity.this, new OnKillApp() {
                        @Override
                        public void call() {
                            logoutDialog.dismiss();
                        }
                    });
                } else if (isRestartAppEvent) {
                    Log.d(BullyElectionP2p.TAG, "Nome alterado");
                    Utils.refreshApp(LoginActivity.this, new OnKillApp() {
                        @Override
                        public void call() {
                            logoutDialog.dismiss();
                        }
                    });
                }
                break;
            case ClientEvent.REGISTRATION_FAIL:
                Log.d(BullyElectionP2p.TAG, "Falha ao registrar o dispositivo com o servidor");
                break;
            case ClientEvent.UNREGISTRATION_FAIL:
                Log.d(BullyElectionP2p.TAG, "Falha ao remover o dispositivo do servidor");
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataTransferEvent(DataTransferEvent dataTransferEvent) {
        switch (dataTransferEvent.event) {
            case DataTransferEvent.DATA_RECEIVED:
                Log.d(BullyElectionP2p.TAG, "Dados recebidos:\n" + dataTransferEvent.data);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        switch (messageEvent.message) {
            case MessageEvent.EXIT_APP:
                Log.d(BullyElectionP2p.TAG, "Reiniciando app");
                LoginActivity.this.recreate();
                break;
        }
    }

    private void checkAdapterIsEmpty() {
        if (professorsListAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            professorsList.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            professorsList.setVisibility(View.VISIBLE);
        }
    }

    private void setupProfessorsList() {
        professorsListAdapter = new ProfessorsListAdapter(bullyElectionP2p.serverDevices, LoginActivity.this);

        professorsListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });

        LinearLayoutManager professorsListLayoutManager = new LinearLayoutManager(LoginActivity.this,
                LinearLayoutManager.VERTICAL, false);

        professorsList.setLayoutManager(professorsListLayoutManager);
        professorsList.setHasFixedSize(true);
        professorsList.setAdapter(professorsListAdapter);
        checkAdapterIsEmpty();
    }

    private void goMainActivity() {
        connectingDialog.hide();
        startActivityForResult(new Intent(LoginActivity.this, MainActivity.class), RESTART_ACTIVITY_REQUEST);
    }

    @Override
    public void onProfessorsListItemClick(BullyElectionP2pDevice professorDevice) {
        connectingDialog = ProgressDialog
                .show(LoginActivity.this, "Conectando...",
                        "Conectando-se ao professor " + professorDevice.readableName);

        bullyElectionP2p.connectToDevice(professorDevice);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(LoginActivity.this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(LoginActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESTART_ACTIVITY_REQUEST) {
            if (resultCode == RESULT_OK) {
                LoginActivity.this.recreate();
            }
        }
    }
}
