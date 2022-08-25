package ch.chassaing.hack;

import java.io.*;
import java.util.BitSet;

public class HackAssembler {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: "+ HackAssembler.class.getSimpleName() +" assemblerfile");
            System.exit(64);
        }

        BitSet bitSet = new BitSet(16);
        bitSet.set(0, true);
        bitSet.set(15, true);
        byte[] bytes = bitSet.toByteArray();

        System.out.println(bytes);
    }


}
