package ehu.mas;

import java.util.Arrays;

public class Test {

  
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    int[] ints = new int[500000000];
    for (int i=0; i< ints.length; i++)
      ints[i]=(int)Math.random()*1000;
    
    long time = System.nanoTime();
    int max = Integer.MIN_VALUE;
    for (int i=0; i< ints.length; i++)
      if (ints[i]>i) max=ints[i];
    System.out.println("max="+max+" "+((System.nanoTime()-time)/1000));
    
    time = System.nanoTime();
    max = Arrays.stream(ints).reduce(Integer.MIN_VALUE, Math::max);
    System.out.println("max="+max+" "+((System.nanoTime()-time)/1000));
    
    time = System.nanoTime();
    max = Arrays.stream(ints).parallel().reduce(Integer.MIN_VALUE, Math::max);
    System.out.println("max="+max+" "+((System.nanoTime()-time)/1000));
  }

}
