/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;

import java.io.File;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

/**
 * QuickFileChooser overrides the updateUI() method of JFileChooser in order to bypass a bug.
 * The bug slows down instantiation of the standard JFileChooser in certain cases, which seem to
 * be affected by native OS virtual filesystem handling of compressed (ZIP or RAR) files in the
 * "comboBox path" (e.g., MyComputer, MyDocuments, Desktop, etc). Use this class for new instances
 * of JFileChooser, as it extends this class and is otherwise identical.
 * @author rnagel
 */
public class QuickFileChooser extends JFileChooser{

    public QuickFileChooser() {
        super();
        this.setFileView(new FileView() {

            @Override
            public Icon getIcon(File f) {
            Icon icon = FileSystemView.getFileSystemView().getSystemIcon(f);
            return icon;
            }
        });
    }

    


    @Override
    public void updateUI() {
        putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);
        super.updateUI();
    }

}
