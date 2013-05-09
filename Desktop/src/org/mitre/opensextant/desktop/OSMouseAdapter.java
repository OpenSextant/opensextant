/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JLabel;

/**
 *
 * @author GBLACK
 */
public class OSMouseAdapter extends MouseAdapter {
    ArrayList list;
    public  OSMouseAdapter(ArrayList list) {
      super();
      this.list = list;
    }
    
    @Override
    public void mouseClicked(MouseEvent e){
      if(e.getClickCount()==2) OpenSextant.viewFileFromRow( e.getSource()
                                                           , list);               
      else OpenSextant.toggleCheck( e.getSource(), list);
    }
        
    @Override
    public void mouseEntered(MouseEvent e)
    {
      OpenSextant.hoverRow(true, e.getSource(), list);
    }
        
    @Override
    public void mouseExited(MouseEvent e)
    {
      OpenSextant.hoverRow(false, e.getSource(), list);
    }
   
         
}
