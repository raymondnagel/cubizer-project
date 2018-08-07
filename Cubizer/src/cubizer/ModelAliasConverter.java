/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;

import java.util.HashMap;

/**
 *
 * @author Raymond
 */
public class ModelAliasConverter {
    private HashMap<String, Model3d> modelMap = new HashMap<>();
    
    public void addAlias(String label, Model3d model)
    {
        modelMap.put(label, model);
    }
    
    public Model3d getAlias(String label)
    {
        return modelMap.get(label);
    }
}
