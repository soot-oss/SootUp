package com.linecorp.centraldogma.server.internal.storage.repository.git;

import java.util.*;
import java.io.*;

public class TrapSerialization {

    public Integer processWithExplicitCasting(String var2, String var4) throws Exception{
        Object var19;
        try {
            try {
                var19 = 10;  // Label1
                throw new Exception();
            } catch (Exception e) {
                var19 = 20;  // Label2
                throw new Exception(e);
            } finally {
                var19 = 30; // Label5
            }
        } catch (Exception ex) {
            var19 = 40;  // Label3
        }
        return (Integer) var19;
    }

}