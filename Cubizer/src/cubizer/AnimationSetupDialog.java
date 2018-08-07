/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;

import java.awt.GridLayout;
import java.util.ArrayList;

/**
 *
 * @author rnagel
 */
public class AnimationSetupDialog extends javax.swing.JDialog implements ShapeDialog{
    private Result result = Result.CANCEL;
    private ArrayList<ModelAliasChooser> modelChoosers = new ArrayList<>();
    
    
    /**
     * Creates new form ConstructLineDialog
     */
    public AnimationSetupDialog(java.awt.Frame parent, boolean modal, ArrayList<String> modelLabels) {
        super(parent, modal);
        initComponents();
        this.setLocationRelativeTo(null);
        
        GridLayout layout = (GridLayout)pnlAliases.getLayout();
        layout.setRows(modelLabels.size());
        for (int m = 0; m < modelLabels.size(); m++)
        {
            ModelAliasChooser chooser = new ModelAliasChooser(modelLabels.get(m));
            modelChoosers.add(chooser);
            pnlAliases.add(chooser);
            chooser.setVisible(true);
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

        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        lblTitle = new javax.swing.JLabel();
        scpAliases = new javax.swing.JScrollPane();
        pnlAliases = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Construct Line");

        btnOk.setText("Ok");
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        lblTitle.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblTitle.setText("Set corresponding models for animation poses:");

        pnlAliases.setLayout(new java.awt.GridLayout(1, 1));
        scpAliases.setViewportView(pnlAliases);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnOk, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
                    .addComponent(scpAliases))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scpAliases, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnOk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancel)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.result = Result.CANCEL;
        setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        this.result = Result.OK;
        setVisible(false);
    }//GEN-LAST:event_btnOkActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlAliases;
    private javax.swing.JScrollPane scpAliases;
    // End of variables declaration//GEN-END:variables

    @Override
    public Result getDialogResult() {
        return this.result;
    }
    public ModelAliasConverter getModelAliasConverter()
    {
        ModelAliasConverter converter = new ModelAliasConverter();
        for (int m = 0; m < modelChoosers.size(); m++)
        {
            converter.addAlias(modelChoosers.get(m).getLabel(), modelChoosers.get(m).getModelChoice());
        }
        return converter;
    }

}