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

	    // Crear el mensaje de solicitud de descarga
	    PeerMessage msg = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD);
	    msg.setHash(targetFileHashSubstr.getBytes());
	    msg.setFile_name(file.getName().getBytes());
	    msg.setNumberOfServersThatHaveFile((byte) nServers);
	    msg.setIdentifierServer((byte) n);

	    // Enviar el mensaje
	    msg.writeMessageToOutputStream(dos);

	    // Recibir los fragmentos del archivo
	    try (FileOutputStream fos = new FileOutputStream(file, true)) {
	        boolean endOfFile = false;
	        while (!endOfFile) {
	            PeerMessage rcv = PeerMessage.readMessageFromInputStream(dis);
	            switch (rcv.getOpcode()) {
	                case PeerMessageOps.OPCODE_FILE:
	                    // Escribir el fragmento en el archivo
	                    byte[] fileData = rcv.getFile_data();
	                    if (fileData != null) {
	                        fos.write(fileData);
	                        System.out.println("[Client] Recibido fragmento de tamaño: " + fileData.length);
	                    } else {
	                        System.err.println("[Client] Fragmento recibido es nulo.");
	                    }
	                    break;

	                case PeerMessageOps.OPCODE_END_OF_FILE:
	                    // Fin del archivo
	                    endOfFile = true;
	                    success = true;
	                    System.out.println("[Client] Fin de archivo recibido.");
	                    break;

	                case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
	                    System.out.println("ERROR: Specified File not found.");
	                    file.delete();
	                    return false;

	                default:
	                    System.err.println("Unexpected opcode received: " + rcv.getOpcode());
	                    return false;
	            }
	        }
	    } catch (IOException e) {
	        System.err.println("[Download] Error durante la descarga: " + e.getMessage());
	        file.delete(); // Borrar archivo incompleto
	        return false;
	    }

	    // Verificar el hash del archivo descargado
	    if (success) {
	        String downloadedFileHash = FileDigest.computeFileChecksumString(file.getAbsolutePath());
	        if (!downloadedFileHash.equals(targetFileHashSubstr)) {
	            System.err.println("[Download] File integrity check failed! Expected hash: " + targetFileHashSubstr
	                    + " but got: " + downloadedFileHash);
	            file.delete(); // Borrar archivo corrupto
	            return false;
	        }
	        System.out.println("[Download] Archivo descargado y verificado correctamente.");
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
