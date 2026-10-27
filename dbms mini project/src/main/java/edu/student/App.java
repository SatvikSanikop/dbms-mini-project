package edu.student;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

public class App {
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    private static final String URL = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/student_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "root");

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/students", App::students);
        server.createContext("/", App::staticFiles);
        server.start();
        System.out.println("Student Record Manager running at http://localhost:" + PORT);
    }

    private static Connection connect() throws SQLException { return DriverManager.getConnection(URL, USER, PASSWORD); }
    private static void students(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            if (method.equals("GET")) {
                List<String> rows = new ArrayList<>();
                try (Connection c = connect(); PreparedStatement p = c.prepareStatement("SELECT student_id,name,email,course,semester FROM students ORDER BY student_id DESC"); ResultSet rs = p.executeQuery()) {
                    while (rs.next()) rows.add("{\"studentId\":"+rs.getInt("student_id")+",\"name\":"+json(rs.getString("name"))+",\"email\":"+json(rs.getString("email"))+",\"course\":"+json(rs.getString("course"))+",\"semester\":"+rs.getInt("semester")+"}");
                }
                send(ex,200,"["+String.join(",",rows)+"]"); return;
            }
            Map<String,String> data = form(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            int id = 0;
            if(method.equals("PUT") || method.equals("DELETE")) {
                try{id=Integer.parseInt(data.getOrDefault("studentId",""));if(id<1)throw new NumberFormatException();}
                catch(NumberFormatException e){throw new IllegalArgumentException("A valid student ID is required.");}
            }
            if(method.equals("DELETE")) {
                try(Connection c=connect(); PreparedStatement p=c.prepareStatement("DELETE FROM students WHERE student_id=?")) {
                    p.setInt(1,id);
                    if(p.executeUpdate()==0){send(ex,404,"{\"error\":\"Student not found.\"}");return;}
                }
                send(ex,200,"{\"message\":\"Student deleted.\"}"); return;
            }
            if(!method.equals("POST") && !method.equals("PUT")){send(ex,405,"{\"error\":\"Method not allowed.\"}");return;}
            String name = required(data,"name",100), email = required(data,"email",150), course = required(data,"course",100);
            int semester; try { semester=Integer.parseInt(data.getOrDefault("semester","")); if(semester<1||semester>12) throw new NumberFormatException(); } catch(NumberFormatException e){ throw new IllegalArgumentException("Semester must be between 1 and 12."); }
            try (Connection c=connect()) {
                if(method.equals("POST")) {
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO students(name,email,course,semester) VALUES(?,?,?,?)")){p.setString(1,name);p.setString(2,email);p.setString(3,course);p.setInt(4,semester);p.executeUpdate();}
                    send(ex,201,"{\"message\":\"Student added.\"}"); return;
                } else if(method.equals("PUT") || method.equals("DELETE")) {
                    try(PreparedStatement p=c.prepareStatement("UPDATE students SET name=?,email=?,course=?,semester=? WHERE student_id=?")) {p.setString(1,name);p.setString(2,email);p.setString(3,course);p.setInt(4,semester);p.setInt(5,id);if(p.executeUpdate()==0){send(ex,404,"{\"error\":\"Student not found.\"}");return;}}
                    send(ex,200,"{\"message\":\"Student updated.\"}"); return;
                }
            }
            send(ex,405,"{\"error\":\"Method not allowed.\"}");
        } catch(IllegalArgumentException e) { send(ex,400,"{\"error\":"+json(e.getMessage())+"}"); }
        catch(SQLException e) { e.printStackTrace(); send(ex,500,"{\"error\":\"Database operation failed. Check the MySQL connection and setup instructions.\"}"); }
    }
    private static String required(Map<String,String>d,String key,int max){String v=d.getOrDefault(key,"").trim();if(v.isEmpty())throw new IllegalArgumentException(key+" is required.");if(v.length()>max)throw new IllegalArgumentException(key+" must be at most "+max+" characters.");if(key.equals("email")&&!v.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw new IllegalArgumentException("Enter a valid email address.");return v;}
    private static Map<String,String> form(String body){Map<String,String>m=new HashMap<>();for(String part:body.split("&")){String[]kv=part.split("=",2);if(kv.length==2)m.put(URLDecoder.decode(kv[0],StandardCharsets.UTF_8),URLDecoder.decode(kv[1],StandardCharsets.UTF_8));}return m;}
    private static String json(String s){return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\"";}
    private static void send(HttpExchange ex,int status,String body)throws IOException{byte[]b=body.getBytes(StandardCharsets.UTF_8);ex.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");ex.getResponseHeaders().set("Access-Control-Allow-Origin","*");ex.sendResponseHeaders(status,b.length);try(var out=ex.getResponseBody()){out.write(b);}}
    private static void staticFiles(HttpExchange ex)throws IOException{String p=ex.getRequestURI().getPath();if(p.equals("/"))p="/index.html";String name=p.substring(1);if(name.contains("..")||name.contains("\\")){ex.sendResponseHeaders(404,-1);return;}try(var in=App.class.getResourceAsStream("/web/"+name)){if(in==null){ex.sendResponseHeaders(404,-1);return;}String type=name.endsWith(".css")?"text/css":name.endsWith(".js")?"text/javascript":"text/html";byte[]b=in.readAllBytes();ex.getResponseHeaders().set("Content-Type",type+"; charset=utf-8");ex.sendResponseHeaders(200,b.length);try(var out=ex.getResponseBody()){out.write(b);}}}
}
