package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import db.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.mapper.HandlerMapper;
import webserver.session.SessionManager;

public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        int port = 0;
        if (args == null || args.length == 0) {
            port = DEFAULT_PORT;
        } else {
            port = Integer.parseInt(args[0]);
        }

        initSchema();
        SessionManager.getInstance();
        HandlerMapper handlerMapper = HandlerMapper.getInstance();
        handlerMapper.initialize();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // 서버소켓을 생성한다. 웹서버는 기본적으로 8080번 포트를 사용한다.
        try (ServerSocket listenSocket = new ServerSocket(port)) {
            logger.info("Web Application Server started {} port.", port);

            // 클라이언트가 연결될때까지 대기한다.
            Socket connection;
            while ((connection = listenSocket.accept()) != null) {
                executor.submit(new RequestHandler(connection));
            }
        }
    }

    private static void initSchema() throws SQLException, IOException {
        try (var conn = ConnectionManager.getConnection();
             var stmt = conn.createStatement();
             var is = WebServer.class.getClassLoader().getResourceAsStream("schema.sql");
             var rd = new BufferedReader(new InputStreamReader(is))) {
            String sql = "", line;
            while ((line = rd.readLine()) != null) {
                sql += line;
                if (line.trim().endsWith(";")) {
                    stmt.execute(sql);
                    sql = "";
                }
            }
        }
    }

}
