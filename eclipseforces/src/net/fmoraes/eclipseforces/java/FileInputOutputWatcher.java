package net.fmoraes.eclipseforces.java;

import java.io.FileReader;
import java.io.IOException;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class FileInputOutputWatcher extends TestWatcher {
  private FileReader inputReader;
  private FileReader outputReader;

  public final FileReader getInputReader()
  {
    return inputReader;
  }
  
  public final FileReader getOutputReader()
  {
    return outputReader;
  }
  
  @Override
  protected void starting(org.junit.runner.Description description)
  {
    try {
      TestInput input = description.getAnnotation(TestInput.class);
      if(input != null) {
        String file = input.fileName();
        inputReader = new FileReader(file);
      }
      TestOutput output = description.getAnnotation(TestOutput.class);
      if(output != null) {
        String file = output.fileName();
        outputReader = new FileReader(file);
      }
    }
    catch(Exception e) {
      throw new RuntimeException(e);
    }

  }
  
  @Override
  protected void finished(Description description)
  {
    try {
      if(inputReader != null)
        inputReader.close();
    }
    catch(IOException e) {
      e.printStackTrace();
    }
    
    try {
      if(outputReader != null)
        outputReader.close();
    }
    catch(IOException e) {
      e.printStackTrace();
    }
    inputReader = null;
    outputReader = null;
  }


}
