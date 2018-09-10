package de.upb.soot.basic.interfaceInvoke;

public class InterfaceInvoke {

    // TODO: check wheter its an interfaceinvoke

    void doMore(){
        System.out.println("..");
    }

    void sth(){

        System.out.println(".");
        this.doMore();

    }

}
