package es.um.redes.nanoFiles.udp.server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/*
	 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */
	private HashMap<InetSocketAddress, FileInfo[]> files;
	private LinkedList<InetSocketAddress> servidoresRegistrados;
	

	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * TODO: (Boletín SocketsUDP) Inicializar el atributo socket: Crear un socket
		 * UDP ligado al puerto especificado por el argumento directoryPort en la
		 * máquina local,
		 */
		socket = new DatagramSocket(DIRECTORY_PORT);
		/*
		 * TODO: (Boletín SocketsUDP) Inicializar atributos que mantienen el estado del
		 * servidor de directorio: ficheros, etc.)
		 */
		files = new HashMap<InetSocketAddress, FileInfo[]>();
		servidoresRegistrados = new LinkedList<>();

		if (NanoFiles.testModeUDP) {
			if (socket == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public DatagramPacket receiveDatagram() throws IOException {
		DatagramPacket datagramReceivedFromClient = null;
		boolean datagramReceived = false;
		while (!datagramReceived) {
			/*
			 * TODO: (Boletín SocketsUDP) Crear un búfer para recibir datagramas y un
			 * datagrama asociado al búfer (datagramReceivedFromClient)
			 */
			byte[] buffer = new byte[DirMessage.PACKET_MAX_SIZE];
			datagramReceivedFromClient = new DatagramPacket(buffer, buffer.length);
			/*
			 * TODO: (Boletín SocketsUDP) Recibimos a través del socket un datagrama
			 */
			socket.receive(datagramReceivedFromClient);


			// Vemos si el mensaje debe ser ignorado (simulación de un canal no confiable)
			double rand = Math.random();
			if (rand < messageDiscardProbability) {
				System.err.println(
						"Directory ignored datagram from " + datagramReceivedFromClient.getSocketAddress());
			} else {
				datagramReceived = true;
				System.out
						.println("Directory received datagram from " + datagramReceivedFromClient.getSocketAddress()
								+ " of size " + datagramReceivedFromClient.getLength() + " bytes.");
			}

		}

		return datagramReceivedFromClient;
	}

	public void runTest() throws IOException {

		System.out.println("[testMode] Directory starting...");

		System.out.println("[testMode] Attempting to receive 'ping' message...");
		DatagramPacket rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);

		System.out.println("[testMode] Attempting to receive 'ping&PROTOCOL_ID' message...");
		rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);
	}

	private void sendResponseTestMode(DatagramPacket pkt) throws IOException {
		/*
		 * TODO: (Boletín SocketsUDP) Construir un String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración.
		 */
		String receivedMessage = new String(pkt.getData(), 0, pkt.getLength());
		String responseMessage;

		/*
		 * TODO: (Boletín SocketsUDP) Después, usar la cadena para comprobar que su
		 * valor es "ping"; en ese caso, enviar como respuesta un datagrama con la
		 * cadena "pingok". Si el mensaje recibido no es "ping", se informa del error y
		 * se envía "invalid" como respuesta.
		 */
		if(receivedMessage.equals("ping")) {
			responseMessage = "pingok";
			
		} else {
			responseMessage = "invalid";
		}
		

		/*
		 * TODO: (Boletín Estructura-NanoFiles) Ampliar el código para que, en el caso
		 * de que la cadena recibida no sea exactamente "ping", comprobar si comienza
		 * por "ping&" (es del tipo "ping&PROTOCOL_ID", donde PROTOCOL_ID será el
		 * identificador del protocolo diseñado por el grupo de prácticas (ver
		 * NanoFiles.PROTOCOL_ID). Se debe extraer el "protocol_id" de la cadena
		 * recibida y comprobar que su valor coincide con el de NanoFiles.PROTOCOL_ID,
		 * en cuyo caso se responderá con "welcome" (en otro caso, "denied").
		 */
	
		responseMessage = new String("Invalid");
		if (receivedMessage.equals("ping")) {
			responseMessage = "pingok";
		    System.out.println("Responding: pingok");
		} else if (receivedMessage.startsWith("ping&")) {
		    String suffix = receivedMessage.substring(5);
		    if (suffix.equals(NanoFiles.PROTOCOL_ID)) {
		    	responseMessage = "welcome";
		        System.out.println("Responding: welcome");
		    } else {
		    	responseMessage = "denied";
		        System.out.println("Responding: denied");
		    }
		}



		byte[] responseData = responseMessage.getBytes();
		DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, pkt.getAddress(), pkt.getPort());
		socket.send(responsePacket);
		
		String messageFromClient = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println("Data received: " + messageFromClient);



	}

	public void run() throws IOException {

		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio
			DatagramPacket rcvDatagram = receiveDatagram();

			sendResponse(rcvDatagram);

		}
	}

	private void sendResponse(DatagramPacket pkt) throws IOException {
		/*
		 * TODO: (Boletín MensajesASCII) Construir String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración. Después, usar la cadena para construir un objeto
		 * DirMessage que contenga en sus atributos los valores del mensaje. A partir de
		 * este objeto, se podrá obtener los valores de los campos del mensaje mediante
		 * métodos "getter" para procesar el mensaje y consultar/modificar el estado del
		 * servidor.
		 */
		String receivedMessageString = new String(pkt.getData(), 0, pkt.getLength());
		DirMessage receivedMessage = DirMessage.fromString(receivedMessageString);
		InetSocketAddress clientAddr = (InetSocketAddress)pkt.getSocketAddress();


		/*
		 * TODO: Una vez construido un objeto DirMessage con el contenido del datagrama
		 * recibido, obtener el tipo de operación solicitada por el mensaje y actuar en
		 * consecuencia, enviando uno u otro tipo de mensaje en respuesta.
		 */
		String operation = DirMessageOps.OPERATION_INVALID; // TODO: Cambiar!
		if(receivedMessage != null) {
			operation = receivedMessage.getOperation();
		}

		/*
		 * TODO: (Boletín MensajesASCII) Construir un objeto DirMessage (msgToSend) con
		 * la respuesta a enviar al cliente, en función del tipo de mensaje recibido,
		 * leyendo/modificando según sea necesario el "estado" guardado en el servidor
		 * de directorio (atributos files, etc.). Los atributos del objeto DirMessage
		 * contendrán los valores adecuados para los diferentes campos del mensaje a
		 * enviar como respuesta (operation, etc.)
		 */

		DirMessage msgToSend = null;
		switch (operation) {
		case DirMessageOps.OPERATION_PING: {

			/*
			 * TODO: (Boletín MensajesASCII) Comprobamos si el protocolId del mensaje del
			 * cliente coincide con el nuestro.
			 */
			String protocolId = receivedMessage.getProtocolId();
			/*
			 * TODO: (Boletín MensajesASCII) Construimos un mensaje de respuesta que indique
			 * el éxito/fracaso del ping (compatible, incompatible), y lo devolvemos como
			 * resultado del método.
			 */
			if(protocolId.equals(NanoFiles.PROTOCOL_ID)) {
				msgToSend = new DirMessage(DirMessageOps.OPERATION_PING_OK);
			}else {
				msgToSend = new DirMessage(DirMessageOps.OPERATION_PING_BAD);

			}
			/*
			 * TODO: (Boletín MensajesASCII) Imprimimos por pantalla el resultado de
			 * procesar la petición recibida (éxito o fracaso) con los datos relevantes, a
			 * modo de depuración en el servidor
			 */
			System.out.println("Ping...received ProtocolId: " + protocolId);

			break;
		}

		
		case DirMessageOps.OPERATION_FILELIST: {
			if(this.files==null || this.files.isEmpty()) {
				msgToSend = new DirMessage(DirMessageOps.OPERATION_FILELIST_RESPONSE, new FileInfo[0]); // Enviar lista vacía
				break;
			}
		
			int totalSize = 0;
			for (FileInfo[] fileInfos : files.values()) {
				totalSize += fileInfos.length;
			}
			FileInfo[] allFiles = new FileInfo[totalSize];
			int currentIndex = 0;
			for (FileInfo[] fileInfos : files.values()) {
				System.arraycopy(fileInfos, 0, allFiles, currentIndex, fileInfos.length);
				currentIndex += fileInfos.length;
			}
			msgToSend = new DirMessage(DirMessageOps.OPERATION_FILELIST_RESPONSE, allFiles);
			break;
		}
		case DirMessageOps.OPERATION_SERVE: {
			// Obtener la ruta completa de la carpeta compartida
		    String sharedFolderPath = NanoFiles.sharedDirname; 
		    // Obtener la lista de archivos en la carpeta compartida en nuestro caso nf-shared
		    File sharedFolder = new File(sharedFolderPath);
		    FileInfo[] files = null;
		    if (sharedFolder.exists() && sharedFolder.isDirectory()) {
		        files = FileInfo.loadFilesFromFolder(sharedFolderPath);
		    } else {
		        System.err.println("* La carpeta compartida no existe o no es una carpeta válida.");
		    }

		    if (files != null && files.length > 0) {
		        System.out.println("* Archivos publicados:");
		        for (FileInfo file : files) {
		            System.out.println("*- " + file);
		        }
		        InetSocketAddress serverAddress = new InetSocketAddress(pkt.getAddress(), receivedMessage.getServerPort());
		        this.files.put(serverAddress, files);
		        if (!servidoresRegistrados.contains(serverAddress)) {
		            servidoresRegistrados.add(serverAddress);
		        }
		    } else {
		        System.out.println("* No se han encontrado archivos para publicar en la carpeta compartida.");
		    }
		    
		    
		    

		    // Construir un mensaje de respuesta indicando si la publicación fue exitosa
		    msgToSend = new DirMessage(DirMessageOps.OPERATION_SERVE_RESPONSE);
		    
		    // Aquí lo que se hace es poner la respuesta del Publish a true en el caso en el que encuentre algún fichero compatible
		    msgToSend.setPublishResponse(files != null && files.length > 0); 
		    break;
		}
		case DirMessageOps.OPERATION_DOWNLOAD: {
		    String filenameSubstring = receivedMessage.getFilenameSubstring();
		    List<InetSocketAddress> matchingServers = new ArrayList<>();
		    for (InetSocketAddress server : servidoresRegistrados) {
		        FileInfo[] serverFiles = files.get(server);
		        if (serverFiles != null) {
		            for (FileInfo file : serverFiles) {
		                if (file.fileName.contains(filenameSubstring)) {
		                    matchingServers.add(server);
		                    break;
		                }
		            }
		        }
		    }

		    if (matchingServers.isEmpty()) {
		        System.out.println("[Directory] No servers found for filename substring: " + filenameSubstring);
		        msgToSend = new DirMessage(DirMessageOps.OPERATION_DOWNLOAD_BAD);
		    } else {
		        System.out.println("[Directory] Found servers for filename substring: " + filenameSubstring);
		        for (InetSocketAddress server : matchingServers) {
		            System.out.println("[Directory] Server: " + server);
		        }
		        InetSocketAddress[] serverList = matchingServers.toArray(new InetSocketAddress[0]);
		        System.out.println("[Directory] Enviando lista de servidores: " + Arrays.toString(serverList));
		        msgToSend = new DirMessage(DirMessageOps.OPERATION_DOWNLOAD_OK, NanoFiles.PROTOCOL_ID);
		        msgToSend.setServerList(serverList);
		    }
		    break;
		}


		default:
			System.err.println("Unexpected message operation: \"" + operation + "\"");
			System.exit(-1);
		}

		/*
		 * TODO: (Boletín MensajesASCII) Convertir a String el objeto DirMessage
		 * (msgToSend) con el mensaje de respuesta a enviar, extraer los bytes en que se
		 * codifica el string y finalmente enviarlos en un datagram pkt
		 */
		if(msgToSend != null) {
			String msgToSendAString = msgToSend.toString();
			System.out.println("Sending response... " + msgToSendAString);
			byte dataToClient[] = msgToSendAString.getBytes();
			DatagramPacket packetToClient = new DatagramPacket(dataToClient, dataToClient.length,clientAddr);
			socket.send(packetToClient);
		}



	}
}
