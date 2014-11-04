package com.aos.rpc.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import com.aos.rpc.dataMarshalling.UDPDemarshaller;
import com.aos.rpc.dataMarshalling.UDPMarshaller;
import com.aos.rpc.helperClasses.ServerSegmentation;

public class UDPClientStreamHandler {
    private int serverUDPPort;
    private String serverIPString;
    private UDPMarshaller[] udpMarshallers;
    private UDPDemarshaller udpDemarshaller;
    private int burstSize;
    //private ByteBuffer[] recievedResultsPackets;
    private int numberOfPacketsToSend;
    //private boolean doneRecieving;
    //private boolean doneSending;
    private DatagramSocket udpClientSocket;

    private int numberOfPacketsToReceive;
    private int reminderOfElements;
    UDPDemarshaller[] recievedResultPackets = null;
    private long TrID;

    public UDPClientStreamHandler(int serverUDPPort, String serverIPString, UDPMarshaller[] udpMarshallers, int elementsToReceive, long tranID)
            throws IOException
    {
        //set the to send
        this.serverUDPPort = serverUDPPort;
        this.serverIPString = serverIPString;
        //toSendParamPackets = new ByteBuffer[numberOfPacketsToSend];
        this.udpMarshallers = udpMarshallers;
        udpDemarshaller = new UDPDemarshaller();
        burstSize = 5;
        numberOfPacketsToSend = udpMarshallers.length;

        udpClientSocket = new DatagramSocket(0);
        udpClientSocket.setSoTimeout(30000);


        //Initialize packets to receive
        reminderOfElements = elementsToReceive % 512;
        if (reminderOfElements == 0)
        {
            numberOfPacketsToReceive = elementsToReceive / 512;
        }
        else
        {
            numberOfPacketsToReceive = elementsToReceive / 512 + 1;
        }

        UDPDemarshaller[] recievedParametersPackets = new UDPDemarshaller[numberOfPacketsToReceive];
        this.TrID = tranID;


    }

    public void recieveUdpResultsPackets() throws IOException
    {
        boolean flag = true;
        int attempts = 0;
        long neededPacket = 1;
        recievedResultPackets = new UDPDemarshaller[numberOfPacketsToReceive];

       UDPDemarshaller demarshaller = new UDPDemarshaller();

        while ((attempts < 3) && ((neededPacket - 1) < numberOfPacketsToReceive))
        {
            for (int i = 0; i < burstSize && flag && (neededPacket <= numberOfPacketsToReceive); i++)
            {
                try
                {
                    byte[] buffer = new byte[4122];
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    udpClientSocket.receive(receivePacket);
                    demarshaller.setStream(getTheRightStream(receivePacket.getData(), receivePacket.getLength()));
                    if (!demarshaller.isCRCError())
                    {
                        if (demarshaller.getSequenceID() == neededPacket)
                        {
                            recievedResultPackets[(int) neededPacket - 1] = demarshaller;
                            demarshaller = new UDPDemarshaller();
                            attempts = 0;
                            neededPacket++;
                        } else
                            flag = false;
                    } else
                        flag = false;
                }
                catch (SocketTimeoutException ste)
                {
                    attempts++;
                    if (attempts == 3)
                        flag = false;
                }
            }
            if (attempts != 3)
            {
                sendAck(neededPacket);
            }
        }

        if (attempts == 3)
        {
            //delete the entry from the status table with TrID (give up the request)
            System.out.println("Error: Time out when receiving the result from server!!!");
            recievedResultPackets = null;
        }
        else if (neededPacket == numberOfPacketsToReceive)
        {
            //I receieved everything perfectly
        //    System.out.println("I receieved result perfectly from the server!!!");
        }
    }

    private void sendAck(long neededPacket) throws IOException
    {
        UDPMarshaller marshaller = new UDPMarshaller();
        marshaller.setTransactionID(TrID);
        marshaller.setSequenceID(neededPacket);
        marshaller.setVectorSize(0);
        marshaller.setType((short)2);
        marshaller.formStream();

        byte[] stream = marshaller.getStream();
        DatagramPacket ack = new DatagramPacket(stream, stream.length, InetAddress.getByName(serverIPString), serverUDPPort);
        udpClientSocket.send(ack);
    }

    private byte[] getTheRightStream(byte[] bufferStream, int length) 
    {
        byte[] result = new byte[length];

        for (int i = 0; i < length; i++)
            result[i] = bufferStream[i];

        return result;
    }



    public void sendUdpParametersPackets() throws IOException 
    {
        boolean flag = true;
        int attempts = 0;
        long packetToSendNum = 0;

        while ((attempts < 3) && (packetToSendNum < numberOfPacketsToSend) && flag) 
        {
            for (int i = 0; i < burstSize; i++) 
            {
                //byte[] stream = new byte[toSendParamPackets[(int)packetToSendNum - 1].capacity()];
                if (packetToSendNum + i >= numberOfPacketsToSend) {
                    packetToSendNum = packetToSendNum + i;
                    break;
                }
                byte[] stream = udpMarshallers[(int) packetToSendNum + i].getStream();
                DatagramPacket packetToSend = new DatagramPacket(stream, stream.length, InetAddress.getByName(serverIPString), serverUDPPort);
                udpClientSocket.send(packetToSend);
            }
            try 
            {
                byte[] buffer = new byte[26];
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                udpClientSocket.receive(receivePacket);
                udpDemarshaller.resetAll();

                udpDemarshaller.setStream(getTheRightStream(receivePacket.getData(), receivePacket.getLength()));

                if (!udpDemarshaller.isCRCError()) {
                    packetToSendNum = udpDemarshaller.getSequenceID() - 1;

                    attempts = 0;
                }
            } catch (SocketTimeoutException ste)
            {
                attempts++;
                if (attempts == 3)
                    flag = false;
            }
        }

        if (attempts == 3) 
        {
            //give up and issue a new request
            //System.out.println("Give up!!!");
            System.out.println("Error: Time out when sending the parameters to server!!!");
            recievedResultPackets = null;


        } else if (packetToSendNum == numberOfPacketsToSend) {
           // System.out.println("Good job!!!");
        }

    }

//
//   // public void setTrID(long TrID)
//    {
//        this.TrID = TrID;
//    }
    public UDPDemarshaller[] getRecievedResultPackets()
    {
        return this.recievedResultPackets;
    }



}