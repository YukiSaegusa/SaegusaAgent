package com.gmail.saegusa41010.tools;

import java.nio.file.Paths;

/**
 * 設定ファイルを読み込ませて公式のゲームランナーを起動するツール
 */
public class Runner {

    public static void main(String[] args) throws Exception {
//        org.aiwolf.ui.bin.AutoStarter.main(new String[]{Paths.get("config", "AutoStarterRef.ini").toAbsolutePath().toString()});
        org.aiwolf.ui.bin.AutoStarter.main(new String[]{Paths.get("config", "AutoStarter.ini").toAbsolutePath().toString()});
    }

}
