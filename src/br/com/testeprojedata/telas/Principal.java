/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.testeprojedata.telas;

import br.com.testeprojedata.dal.ModuloConexao;
import classes.Funcionario;
import classes.ImportarData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import net.proteanit.sql.DbUtils;

/**
 *
 * @author Luciano & Paty
 */
public class Principal extends javax.swing.JFrame {

    //Usando a variavel de conexao do DAL
    Connection conexao = null;
    //Criando variaveis especiais para conexao com o banco 
    //Prepared Stetament e ResultSet são Frameworks do pacote java.sql
    // e servem para preparar e executar as instruções SQL
    PreparedStatement pst = null;
    ResultSet rs = null;

    List<Funcionario> listaDeFuncionarios;
    DateTimeFormatter dtf;

    /**
     * Creates new form TelaFuncionarios
     */
    public Principal() {
        initComponents();
        conexao = ModuloConexao.conector();
        ImportarData importarData = new ImportarData();

        inserioFuncionarios();

        todosFuncionarios();

        importarData.preenchendoListaDeFuncionarios();
        importarData.imprimirLista(true);

        // 3.4 Aumento de Salario (10%)
        importarData.atualizarSalario(10);

        // 3.5 Funcionários por função em um MAP, sendo a chave a “função” e o valor a “lista de funcionários”.
        // 3.6 Imprimir os funcionários, agrupados por função.
        importarData.imprimirPorFuncao();

        funcionarioMaisVelho();
        totalDosSalarios();
    }

    public void inserioFuncionarios() {

        String caminhoArquivo = "src/funcionarios.csv";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d/MM/yyyy");

        try {
            FileReader leitorArquivo = new FileReader(caminhoArquivo);
            BufferedReader br = new BufferedReader(leitorArquivo);

            String linha = br.readLine();
            linha = br.readLine();

            while (linha != null) {
                String[] valor = linha.split(",");

                String nome = valor[0];
                String dataNascimento = valor[1];
                LocalDate localDate = LocalDate.parse(dataNascimento, dtf);
                BigDecimal salario = new BigDecimal(valor[2].replace(",", "."));
                String funcao = valor[3];

                String dia = dataNascimento.substring(0, 2);
                String mes = dataNascimento.substring(3, 5);
                String ano = dataNascimento.substring(6);

                String datamysql = ano + "-" + mes + "-" + dia;

                String sql = "insert into tbpessoa(nome, data_nascimento, salario, funcao) values(?,?,?,?);";
                try {
                    pst = conexao.prepareStatement(sql);

                    pst.setString(1, nome);
                    pst.setString(2, datamysql);
                    pst.setString(3, String.valueOf(salario));
                    pst.setString(4, funcao);

                    int adicionado = pst.executeUpdate();

                    if (adicionado > 0) {

                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e);
                }

                linha = br.readLine();
            }

            JOptionPane.showMessageDialog(null, "Funcionarios adicionados com sucesso!");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

   
    private void todosFuncionarios() {

        String sql = "select nome as Nome, date_format(data_nascimento,'%d/%m/%Y') as Data_Nascimento, funcao as Função, Concat(  \n"
                + "                          Replace  \n"
                + "                           (Replace  \n"
                + "                                 (Replace  \n"
                + "                                  (Format(salario, 3), '.', '|'), ',', '.'), '|', ',')) AS Salario from tbpessoa order by id_pessoa";

        try {
            pst = conexao.prepareStatement(sql);
            //Passando o conteudo da caixa de pesquisa para o ?
            //atenção ao "%" - continuação da pesquisa sql
            //pst.setString(1, checkinativo);
            //pst.setString(2, txtAssocPesquisar.getText() + "%");
            rs = pst.executeQuery();
            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela 
            tblListaFuncionarios.setModel(DbUtils.resultSetToTableModel(rs));
            tblListaFuncionarios.getColumnModel().getColumn(0).setMaxWidth(80);
            tblListaFuncionarios.getColumnModel().getColumn(1).setMaxWidth(120);
            tblListaFuncionarios.getColumnModel().getColumn(2).setMaxWidth(100);
            tblListaFuncionarios.getColumnModel().getColumn(3).setMaxWidth(80);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void todosFuncionariosCom10PorCento() {

        String sql = "select nome as Nome, date_format(data_nascimento,'%d/%m/%Y') as Data_Nascimento, funcao as Função, Concat(  \n"
                + "                          Replace  \n"
                + "                           (Replace  \n"
                + "                                 (Replace  \n"
                + "                                  (Format((salario + (salario *10/100)), 3), '.', '|'), ',', '.'), '|', ',')) AS Salario from tbpessoa;";

        try {
            pst = conexao.prepareStatement(sql);
            //Passando o conteudo da caixa de pesquisa para o ?
            //atenção ao "%" - continuação da pesquisa sql
            //pst.setString(1, checkinativo);
            //pst.setString(2, txtAssocPesquisar.getText() + "%");
            rs = pst.executeQuery();
            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela 
            tblListaFuncionarios.setModel(DbUtils.resultSetToTableModel(rs));
            tblListaFuncionarios.getColumnModel().getColumn(0).setMaxWidth(80);
            tblListaFuncionarios.getColumnModel().getColumn(1).setMaxWidth(120);
            tblListaFuncionarios.getColumnModel().getColumn(2).setMaxWidth(100);
            tblListaFuncionarios.getColumnModel().getColumn(3).setMaxWidth(80);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    public void funcionarioMaisVelho() {

        String sql = "select nome as Nome, (2024 - year(DATA_NASCIMENTO)) as Idade from tbpessoa order by year(DATA_NASCIMENTO) limit 1;";

        try {
            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();
            if (rs.next()) {

                lblNome.setText("Funcionário(a) mais velho: " + rs.getString(1) + " com ");
                lblIdade.setText(rs.getString(2) + " anos.");

            }
        } catch (SQLException e2) {
            JOptionPane.showMessageDialog(null, e2);
        }

    }

    public void totalDosSalarios() {

        String sql = "select Concat(  Replace  (Replace (Replace (Format(sum(salario), 3), '.', '|'), ',', '.'), '|', ',')) AS Salario from tbpessoa;";

        try {
            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();
            if (rs.next()) {

                lblSalarioTotal.setText("Total do sálario dos funcioanarios é de " + rs.getString(1) + " R$.");

            }
        } catch (SQLException e2) {
            JOptionPane.showMessageDialog(null, e2);
        }

    }

    private void listaFuncionariosPorOrdemAlfabetica() {

        String sql = "select nome as Nome, date_format(data_nascimento,'%d/%m/%Y') as Data_Nascimento, funcao as Função, Concat(  \n"
                + "                          Replace  \n"
                + "                           (Replace  \n"
                + "                                 (Replace  \n"
                + "                                  (Format(salario, 3), '.', '|'), ',', '.'), '|', ',')) AS Salario from tbpessoa order by nome";

        try {
            pst = conexao.prepareStatement(sql);
            //Passando o conteudo da caixa de pesquisa para o ?
            //atenção ao "%" - continuação da pesquisa sql
            //pst.setString(1, checkinativo);
            //pst.setString(2, txtAssocPesquisar.getText() + "%");
            rs = pst.executeQuery();
            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela 
            tblListaFuncionarios.setModel(DbUtils.resultSetToTableModel(rs));
            tblListaFuncionarios.getColumnModel().getColumn(0).setMaxWidth(80);
            tblListaFuncionarios.getColumnModel().getColumn(1).setMaxWidth(120);
            tblListaFuncionarios.getColumnModel().getColumn(2).setMaxWidth(100);
            tblListaFuncionarios.getColumnModel().getColumn(3).setMaxWidth(80);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void todosFuncionariosNascidosNoMes10E12() {

        String sql = "SELECT nome as Nome, date_format(data_nascimento,'%d/%m/%Y') as Data_Nascimento, funcao as Função, Concat(  \n"
                + "                          Replace  \n"
                + "                           (Replace  \n"
                + "                                 (Replace  \n"
                + "                                  (Format((salario + (salario *10/100)), 3), '.', '|'), ',', '.'), '|', ',')) AS Salario FROM tbpessoa WHERE MONTH(DATA_NASCIMENTO) = 10 OR MONTH(DATA_NASCIMENTO) = 12;";

        try {
            pst = conexao.prepareStatement(sql);
            //Passando o conteudo da caixa de pesquisa para o ?
            //atenção ao "%" - continuação da pesquisa sql
            //pst.setString(1, checkinativo);
            //pst.setString(2, txtAssocPesquisar.getText() + "%");
            rs = pst.executeQuery();
            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela 
            tblListaFuncionarios.setModel(DbUtils.resultSetToTableModel(rs));
            tblListaFuncionarios.getColumnModel().getColumn(0).setMaxWidth(80);
            tblListaFuncionarios.getColumnModel().getColumn(1).setMaxWidth(120);
            tblListaFuncionarios.getColumnModel().getColumn(2).setMaxWidth(0);
            tblListaFuncionarios.getColumnModel().getColumn(3).setMaxWidth(0);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void totalDeSalarioMinimo() {

        String sql = "select nome as Nome, Concat(  Replace  (Replace (Replace (Format((salario /1212), 2), '.', '|'), ',', '.'), '|', ',')) AS Salario from tbpessoa;";

        try {
            pst = conexao.prepareStatement(sql);

            rs = pst.executeQuery();
            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela 
            tblListaFuncionarios.setModel(DbUtils.resultSetToTableModel(rs));
            tblListaFuncionarios.getColumnModel().getColumn(0).setMaxWidth(80);
            tblListaFuncionarios.getColumnModel().getColumn(1).setMaxWidth(120);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void removerJoao() {

        String sql = "delete from tbpessoa where nome = 'João';";

        try {
            pst = conexao.prepareStatement(sql);
            //pst.setString(1, datainativacao);
            //pst.setString(2, txtIdVeic.getText());
            int apagado = pst.executeUpdate();

            if (apagado > 0) {
                JOptionPane.showMessageDialog(null, "João removido da lista.");

                todosFuncionarios();

                JOptionPane.showMessageDialog(null, "Lista atualizada.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblListaFuncionarios = new javax.swing.JTable();
        btnRemoverJoao = new javax.swing.JButton();
        listaCom10DeAumento = new javax.swing.JButton();
        listEmOrdemAlfabetica = new javax.swing.JButton();
        btnNascMes10E12 = new javax.swing.JButton();
        btnMaisVelho = new javax.swing.JButton();
        lblNome = new javax.swing.JLabel();
        lblIdade = new javax.swing.JLabel();
        lblSalarioTotal = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Lista de funcionarios"));

        tblListaFuncionarios = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }
        };
        tblListaFuncionarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Nome", "Data Nascimento", "Função", "Sálario"
            }
        ));
        jScrollPane2.setViewportView(tblListaFuncionarios);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 391, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnRemoverJoao.setText("Remover João");
        btnRemoverJoao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoverJoaoActionPerformed(evt);
            }
        });

        listaCom10DeAumento.setText("Lista com 10% de aumento no sálario]");
        listaCom10DeAumento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listaCom10DeAumentoActionPerformed(evt);
            }
        });

        listEmOrdemAlfabetica.setText("Lista por ordem alfabética");
        listEmOrdemAlfabetica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listEmOrdemAlfabeticaActionPerformed(evt);
            }
        });

        btnNascMes10E12.setText("Aniversariantes dos mês 10 e 12");
        btnNascMes10E12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNascMes10E12ActionPerformed(evt);
            }
        });

        btnMaisVelho.setText("Quantos salarios minimos cada funcionario quanhar");
        btnMaisVelho.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMaisVelhoActionPerformed(evt);
            }
        });

        lblNome.setText("jLabel2");

        lblIdade.setText("jLabel2");

        lblSalarioTotal.setText("jLabel2");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(163, 163, 163)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(132, 132, 132)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnRemoverJoao)
                                .addGap(18, 18, 18)
                                .addComponent(listaCom10DeAumento)
                                .addGap(18, 18, 18)
                                .addComponent(listEmOrdemAlfabetica))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnNascMes10E12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnMaisVelho))
                            .addComponent(lblSalarioTotal)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblNome)
                                .addComponent(lblIdade)))))
                .addContainerGap(137, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRemoverJoao)
                    .addComponent(listaCom10DeAumento)
                    .addComponent(listEmOrdemAlfabetica))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNascMes10E12)
                    .addComponent(btnMaisVelho))
                .addGap(70, 70, 70)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNome)
                    .addComponent(lblIdade))
                .addGap(18, 18, 18)
                .addComponent(lblSalarioTotal)
                .addContainerGap(321, Short.MAX_VALUE))
        );

        setBounds(0, 0, 794, 853);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRemoverJoaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoverJoaoActionPerformed
        removerJoao();

    }//GEN-LAST:event_btnRemoverJoaoActionPerformed

    private void listaCom10DeAumentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listaCom10DeAumentoActionPerformed
        todosFuncionariosCom10PorCento();
    }//GEN-LAST:event_listaCom10DeAumentoActionPerformed

    private void listEmOrdemAlfabeticaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listEmOrdemAlfabeticaActionPerformed
        listaFuncionariosPorOrdemAlfabetica();
    }//GEN-LAST:event_listEmOrdemAlfabeticaActionPerformed

    private void btnNascMes10E12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNascMes10E12ActionPerformed
        todosFuncionariosNascidosNoMes10E12();
    }//GEN-LAST:event_btnNascMes10E12ActionPerformed

    private void btnMaisVelhoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMaisVelhoActionPerformed
        totalDeSalarioMinimo();
    }//GEN-LAST:event_btnMaisVelhoActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Principal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Principal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Principal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Principal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Principal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMaisVelho;
    private javax.swing.JButton btnNascMes10E12;
    private javax.swing.JButton btnRemoverJoao;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblIdade;
    private javax.swing.JLabel lblNome;
    private javax.swing.JLabel lblSalarioTotal;
    private javax.swing.JButton listEmOrdemAlfabetica;
    private javax.swing.JButton listaCom10DeAumento;
    private javax.swing.JTable tblListaFuncionarios;
    // End of variables declaration//GEN-END:variables
}
