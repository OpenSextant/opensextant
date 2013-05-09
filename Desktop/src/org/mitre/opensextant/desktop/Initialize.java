/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop;

import org.mitre.opensextant.apps.OpenSextantRunner;

/**
 *
 * @author GBLACK
 */
public class Initialize implements Runnable {
  private static boolean isInitialized = false;
  private static boolean startedInit = false;
  
  public static boolean getInitialized() { return isInitialized; }

  public void run() {
    try{ (new OpenSextantRunner()).initialize(); }
    catch (Exception e) { e.printStackTrace(); } 
      
    Initialize.isInitialized = true;
  }
  
  public static void init(){
    if(startedInit) return;
    startedInit = true;
 
    (new Thread(new Initialize())).start();
  }

}
