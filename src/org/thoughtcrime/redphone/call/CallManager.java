/*
 * Copyright (C) 2011 Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.thoughtcrime.redphone.call;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import org.thoughtcrime.redphone.audio.AudioException;
import org.thoughtcrime.redphone.audio.CallAudioManager;
import org.thoughtcrime.redphone.datagraham.CustomSocket;
import org.thoughtcrime.redphone.datagraham.CustomToDatagramPipe;
import org.thoughtcrime.redphone.datagraham.DatagramToCustomPipe;
import org.thoughtcrime.redphone.datagraham.DataGrahamSocket;
import org.thoughtcrime.redphone.crypto.SecureRtpSocket;
import org.thoughtcrime.redphone.crypto.zrtp.MasterSecret;
import org.thoughtcrime.redphone.crypto.zrtp.SASInfo;
import org.thoughtcrime.redphone.crypto.zrtp.ZRTPSocket;
import org.thoughtcrime.redphone.signaling.SessionDescriptor;
import org.thoughtcrime.redphone.signaling.SignalingSocket;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;


/**
 * The base class for both Initiating and Responder call
 * managers, which coordinate the setup of an outgoing or
 * incoming call.
 *
 * @author Moxie Marlinspike
 *
 */

public abstract class CallManager extends Thread {

  private static final String TAG = CallManager.class.getSimpleName();

  protected final String            remoteNumber;
  protected final CallStateListener callStateListener;
  protected final Context           context;

  private   boolean          terminated;
  protected CallAudioManager callAudioManager;
  private SignalManager signalManager;
  private SASInfo sasInfo;
  private   boolean          muteEnabled;
  private   boolean          callConnected;

  protected SessionDescriptor sessionDescriptor;
  protected ZRTPSocket zrtpSocket;
  protected SecureRtpSocket secureSocket;
  protected SignalingSocket signalingSocket;

  protected CustomToDatagramPipe customToDatagram;
  protected DatagramToCustomPipe datagramToCustom;

  public CallManager(Context context, CallStateListener callStateListener,
                     String remoteNumber, String threadName)
  {
    super(threadName);
    this.remoteNumber      = remoteNumber;
    this.callStateListener = callStateListener;
    this.terminated        = false;
    this.context           = context;
  }

  @Override
  public void run() {
    Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

    try {
//      Log.d(TAG, "negotiating...");
//      if (!terminated) {
//        zrtpSocket.negotiateStart();
//      }
//
//      if (!terminated) {
//        callStateListener.notifyPerformingHandshake();
//        zrtpSocket.negotiateFinish();
//      }
//
//      if (!terminated) {
//        sasInfo = zrtpSocket.getSasInfo();
//        callStateListener.notifyCallConnected(sasInfo);
//      }
      DataGrahamSocket dataGrahamSocket = new DataGrahamSocket();
      CustomSocket customSocket = new CustomSocket(dataGrahamSocket);
      customToDatagram = new CustomToDatagramPipe(
              zrtpSocket.getDatagramSocket(), customSocket,
              zrtpSocket.getRemoteIp(),zrtpSocket.getRemotePort());
      datagramToCustom = new DatagramToCustomPipe(
              zrtpSocket.getDatagramSocket(), customSocket);
      customToDatagram.start();
      datagramToCustom.start();

//      if (!terminated) {
//        Log.d(TAG, "Finished handshake, calling run() on CallAudioManager...");
//        callConnected = true;
//        runAudio(zrtpSocket.getDatagramSocket(), zrtpSocket.getRemoteIp(),
//                 zrtpSocket.getRemotePort(), zrtpSocket.getMasterSecret(), muteEnabled);
//      }

//    } catch (RecipientUnavailableException rue) {
//      Log.w(TAG, rue);
//      if (!terminated) callStateListener.notifyRecipientUnavailable();
//    } catch (NegotiationFailedException nfe) {
//      Log.w(TAG, nfe);
//      if (!terminated) callStateListener.notifyHandshakeFailed();
//    } catch (AudioException e) {
//      Log.w(TAG, e);
//      callStateListener.notifyClientError(e.getClientMessage());
    } catch (IOException e) {
      Log.w(TAG, e);
      callStateListener.notifyCallDisconnected();
    }
  }

  public void terminate() {
    this.terminated = true;

    if (callAudioManager != null)
      callAudioManager.terminate();

    if (signalManager != null)
      signalManager.terminate();

    if (zrtpSocket != null)
      zrtpSocket.close();

    if (customToDatagram != null)
      customToDatagram.stop();

    if (datagramToCustom != null)
      datagramToCustom.stop();
  }

  public SessionDescriptor getSessionDescriptor() {
    return this.sessionDescriptor;
  }

  public SASInfo getSasInfo() {
    return this.sasInfo;
  }

  protected void processSignals() {
    Log.w(TAG, "Starting signal processing loop...");
    this.signalManager = new SignalManager(callStateListener, signalingSocket, sessionDescriptor);
  }

  protected abstract void runAudio(DatagramSocket datagramSocket, String remoteIp, int remotePort,
                                   MasterSecret masterSecret, boolean muteEnabled)
      throws SocketException, AudioException;


  public void setMute(boolean enabled) {
    muteEnabled = enabled;
    if (callAudioManager != null) {
      callAudioManager.setMute(muteEnabled);
    }
    if (customToDatagram != null) {
      customToDatagram.setMuteEnabled(muteEnabled);
    }
  }

  /**
   * Did this call ever successfully complete SRTP setup
   * @return true if the call connected
   */
  public boolean callConnected() {
    return callConnected;
  }

}
