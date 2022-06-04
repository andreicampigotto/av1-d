/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package ifc.edu.br.mvcookielogin.model;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author friend
 */
@WebServlet(name = "Geral", urlPatterns = {"/Geral"})
public class Geral extends HttpServlet {

    private String retornarCookieLogin(HttpServletRequest request) {
        Cookie listaCookies[] = request.getCookies();
        if (listaCookies != null) {
            for (Cookie c : listaCookies) {
                if (c.getName().equals("mvCookieLogin-1-login")) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // inicia a sessão
        HttpSession sessao = request.getSession(true);

        String op = request.getParameter("op");
        if (op == null) {
            if (sessao.getAttribute("login") == null) { // está desconectado?
                String loginNoCookie = retornarCookieLogin(request);
                if (loginNoCookie != null) {
                    // só faz essa ação 1 vez: tem no cookie, mas não tem na sessão
                    // problema: verifica toda vez se tem atributo na sessão
                    // workaround: usar op=inicio nos direcionamentos, 
                    // em vez de apenas chamar o servlet Geral (op=null)
                    // adiciona login na sessao
                    sessao.setAttribute("login", loginNoCookie);
                    sessao.setAttribute("mensagem", "Bem vindo de volta, você CONTINUA logado!");
                }
            }
            getServletContext().getRequestDispatcher("/inicial.jsp").forward(request, response);
        } else if (op.equals("inicio")) {
            getServletContext().getRequestDispatcher("/inicial.jsp").forward(request, response);
        } else if (op.equals("formlogin")) {
            getServletContext().getRequestDispatcher("/formlogin.jsp").forward(request, response);
        } else if (op.equals("logout")) {
            sessao.removeAttribute("login"); // remove login da sessão

            // remove o cookie
            Cookie ckLogin = new Cookie("mvCookieLogin-1-login", "");
            ckLogin.setMaxAge(0);
            response.addCookie(ckLogin);

            // usa redirect para, após o clique, 
            // não permanece na barra de endereços: Geral?op=logout
            response.sendRedirect(request.getContextPath() + "/Geral");
            //getServletContext().getRequestDispatcher("/formlogin.jsp").forward(request, response);
        } else if (op.equals("cadastroPessoas")) {
            getServletContext().getRequestDispatcher("/cadastroPessoas.jsp").forward(request, response);
        } else {
            getServletContext().getRequestDispatcher("/inicial.jsp").forward(request, response);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // inicia a sessão
        HttpSession sessao = request.getSession(true);

        String op = request.getParameter("op");
        if (op == null) {
            getServletContext().getRequestDispatcher("/inicial.jsp").forward(request, response);
        } else if (op.equals("login")) {
            String login = request.getParameter("login");
            String senha = request.getParameter("senha");
            boolean logado;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            try {
                
                Connection c = (Connection) DriverManager.getConnection("jdbc:mysql://51.222.203.22:3306/hylsonco_poo2", "hylsonco_poo2_user", "ifcPoo2!#$");
                Statement stmt = c.createStatement();
                stmt.executeUpdate("create table if not exists usuario (id integer not null, nome varchar(255), senha varchar(255), primary key (id))");
                stmt.executeUpdate("delete from usuario");
                stmt.executeUpdate("insert into usuario (id, nome, senha) values (1, 'root', MD5('testando'))");
                PreparedStatement ps = c.prepareStatement("select count(1) from usuario where nome = ? and senha = MD5(?)");
                ps.setString(1, login);
                ps.setString(2, senha);
                ResultSet rs = ps.executeQuery();
                rs.next();
                logado = rs.getInt(1) > 0;
            } catch (SQLException ex) {
                Logger.getLogger(Geral.class.getName()).log(Level.SEVERE, null, ex);
                logado = false;
                ex.printStackTrace();
            }
            if (logado) {
                sessao.setAttribute("login", login);
                sessao.setAttribute("mensagem", "Login efetuado com sucesso");

                // guarda o login em cookie, para permanecer logado se fechar o browser
                Cookie ckLogin = new Cookie("mvCookieLogin-1-login", login);
                ckLogin.setMaxAge(24 * 60 * 60); // permanece por 1 dia
                //ckLogin.setPath("/");
                response.addCookie(ckLogin);

                // usa redirect para, após o clique, 
                // não permanece na barra de endereços: Geral?op=logout
                response.sendRedirect(request.getContextPath() + "/Geral");
            } else {
                sessao.setAttribute("mensagem", "Erro no login e/ou senha, tente novamente.");
                getServletContext().getRequestDispatcher("/formlogin.jsp").forward(request, response);
            }
        } else {
            getServletContext().getRequestDispatcher("/inicial.jsp").forward(request, response);
    }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
