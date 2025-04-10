package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDigest;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;

	DataInputStream dis  = null;
	DataOutputStream dos = null;

	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		serverAddr = fserverAddr;
		/*
		 * TODO: (Boletín SocketsTCP) Se crea el socket a partir de la dirección del
		 * servidor (IP, puerto). La creación exitosa del socket significa que la
		 * conexión TCP ha sido establecida.
		 */
		socket = new Socket(serverAddr.getAddress(), serverAddr.getPort());
		/*
		 * TODO: (Boletín SocketsTCP) Se crean los DataInputStream/DataOutputStream a
		 * partir de los streams de entrada/salida del socket creado. Se usarán para
		 * enviar (dos) y recibir (dis) datos del servidor.
		 */
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		
		


	}
	
	public boolean downloadFileChunk(String targetFileHashSubstr, File file, int n, int nServers) throws IOException {
		boolean success = false;
		System.out.println("Starting download");
		//Creacion del mensaje download
		PeerMessage msg = new PeerMessage (PeerMessageOps.OPCODE_DOWNLOAD);
		msg.setHash(targetFileHashSubstr.getBytes());
		msg.setFile_name(file.getName().getBytes());
		msg.setNumberOfServersThatHaveFile((byte) nServers);
		msg.setIdentifierServer((byte) n);
		//Envio del mensaje
		System.out.println("[Client] Hash enviado: " + targetFileHashSubstr);
		msg.writeMessageToOutputStream(dos);
		//Recepcion del mensaje de confirmacion
		PeerMessage rcv = PeerMessage.readMessageFromInputStream(dis);
		switch (rcv.getOpcode()) {
		
		//La descarga se ha realizado correctamente 
		case PeerMessageOps.OPCODE_FILE:
			success = true;
			byte[] filehash = rcv.getHash();
			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(rcv.getDownloadedFile());
			fos.close();	
			if (n==nServers) {
				success = FileDigest.computeFileChecksumString(file.getAbsolutePath()).equals(filehash);
				if (success) {
					System.out.println("Succesfully downloaded remote file to " + file.getAbsolutePath());
					System.out.println("File '"+ file.getName() + "' downloaded succesfully.");
				}
			
			}
			break;
			
			
		//File hash pasado no identifica a ningun fichero del servidor de ficheros
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			System.out.println("ERROR: Especified File not found.");
			file.delete();
			break;
		
		
		
		}
		
		
		return success;
	}

	public void test() {
		/*
		 * TODO: (Boletín SocketsTCP) Enviar entero cualquiera a través del socket y
		 * después recibir otro entero, comprobando que se trata del mismo valor.
		 */
		
		
		try {
			int integerTsend = 1;
			int integerresived;
			dos.writeInt(integerTsend);
			integerresived = dis.readInt();
			
		}
		catch (Exception e) {

		}
	}





	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
