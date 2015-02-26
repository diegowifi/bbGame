/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bbGame;

public class AnimaColision extends Animacion {
    private boolean bColision;
    
    //Se crea constructor con el constructor Heredado
    public AnimaColision(){
        super();
        bColision = false;
    }
    
    //Set para asignar colision
    public void setColision(boolean bCol){
        bColision = bCol;
    }
    
//regresa la colision
    public boolean isColision(){
        return bColision;
    }
}
