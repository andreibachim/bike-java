package io.github.andreibachim.bike.services.bluetooth.adapter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AdapterStateListener {
  private final Runnable powerOnHandler;
  private final Runnable powerOffHandler; 

  public void powerOn() {
    powerOnHandler.run();
  }
  public void powerOff() {
    powerOffHandler.run();
  }
}
