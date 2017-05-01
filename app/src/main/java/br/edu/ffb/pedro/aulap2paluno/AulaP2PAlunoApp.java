package br.edu.ffb.pedro.aulap2paluno;

import android.app.Application;

import org.greenrobot.greendao.database.Database;

import br.edu.ffb.pedro.aulap2paluno.model.DaoMaster;
import br.edu.ffb.pedro.aulap2paluno.model.DaoSession;
import br.edu.ffb.pedro.bullyelectionp2p.BullyElectionP2p;

public class AulaP2PAlunoApp extends Application {

    private BullyElectionP2p bullyElectionP2p;
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "student.db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public BullyElectionP2p initBullyElectionP2p(String readableName) {
        bullyElectionP2p = new BullyElectionP2p(this, readableName);
        return bullyElectionP2p;
    }

    public BullyElectionP2p getBullyElectionP2p() {
        return bullyElectionP2p;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
