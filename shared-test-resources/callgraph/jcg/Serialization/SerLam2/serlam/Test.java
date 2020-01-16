package serlam;
import java.io.Serializable;
public @FunctionalInterface interface Test extends Serializable{
    String concat(Integer seconds);
}
