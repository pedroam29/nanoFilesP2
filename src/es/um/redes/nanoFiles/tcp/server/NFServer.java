package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServer implements Runnable {

	public static final int PORT = 10000;

	private ServerSocket serverSocket = null;
	private boolean stopServer = false;

	public NFServer() throws IOException {
		/*
		 * TODO: (Boletín SocketsTCP) Crear una direción de socket a partir del puerto
		 * especificado (PORT)
		 */
		InetSocketAddress serverSocketAddres = new InetSocketAddress(PORT);
		/*
		 * TODO: (Boletín SocketsTCP) Crear un socket servidor y ligarlo a la dirección
		 * de socket anterior
		 */
		serverSocket = new ServerSocket();
		serverSocket.bind(serverSocketAddres);
	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación.
	 * 
	 */
	public void test() {
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println(
					"[fileServerTestMode] Failed to run file server, server socket is null or not bound to any port");
			return;
		} else {
			System.out
					.println("[fileServerTestMode] NFServer running on " + serverSocket.getLocalSocketAddress() + ".");
		}

		while (true) {
			/*
			 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
			 * otros peers que soliciten descargar ficheros.
			 */
			boolean connectionOk = false;
			Socket socket = null;

			try {
				socket = serverSocket.accept();
				connectionOk = true;
			} catch (Exception e) {
				System.err.println("Connection refused");

			}

			/*
			 * TODO: (Boletín SocketsTCP) Tras aceptar la conexión con un peer cliente, la
			 * comunicación con dicho cliente para servir los ficheros solicitados se debe
			 * implementar en el método serveFilesToClient, al cual hay que pasarle el
			 * socket devuelto por accept.
			 */

			if (connectionOk) {

				try {

					DataInputStream dis = new DataInputStream(socket.getInputStream());
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
					int integerRecived = dis.readInt();
					System.out.println("Entero recibido");
					int integertosend = integerRecived + 1;
					dos.writeInt(integertosend);
					System.out.println("Entero enviado");
				} catch (Exception e) {

				}

			}

		}
	}

	/**
	 * Método que ejecuta el hilo principal del servidor en segundo plano, esperando
	 * conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		/*
		 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
		 * otros peers que soliciten descargar ficheros
		 */

		/*
		 * TODO: (Boletín SocketsTCP) Al establecerse la conexión con un peer, la
		 * comunicación con dicho cliente se hace en el método
		 * serveFilesToClient(socket), al cual hay que pasarle el socket devuelto por
		 * accept
		 */
		/*
		 * TODO: (Boletín TCPConcurrente) Crear un hilo nuevo de la clase
		 * NFServerThread, que llevará a cabo la comunicación con el cliente que se
		 * acaba de conectar, mientras este hilo vuelve a quedar a la escucha de
		 * conexiones de nuevos clientes (para soportar múltiples clientes). Si este
		 * hilo es el que se encarga de atender al cliente conectado, no podremos tener
		 * más de un cliente conectado a este servidor.
		 */
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println("[NFServer] Server socket is null or not bound.");
			return;
		}
		System.out.println("[NFServer] Server running en el puerto " + serverSocket.getLocalPort());
		while (!stopServer) {
			try {
				// Esperar conexiones de clientes
				Socket clientSocket = serverSocket.accept();
				System.out.println("[NFServer] New client connected: " + clientSocket.getInetAddress());

				// Crear un hilo para manejar la conexión con el cliente
				NFServerThread clientThread = new NFServerThread(clientSocket);
				clientThread.start();
			} catch (IOException e) {
				if (stopServer) {
					System.out.println("[NFServer] Server shutting down...");
				} else {
					System.err.println("[NFServer] Error accepting client connection: " + e.getMessage());
				}
			}
		}

	}
	/*
	 * TODO: (Boletín SocketsTCP) Añadir métodos a esta clase para: 1) Arrancar el
	 * servidor en un hilo nuevo que se ejecutará en segundo plano 2) Detener el
	 * servidor (stopserver) 3) Obtener el puerto de escucha del servidor etc.
	 */

	public void startServer() {
		new Thread(this).start();
	}

	public void stopserver() {
		stopServer = true;
		try {

			serverSocket.close();
			System.out.println("* Servidor detenido.");
		} catch (IOException e) {
			System.err.println("* Error al detener el servidor: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public int getPort() {
		return serverSocket.getLocalPort();
	}

	/**
	 * Método de clase que implementa el extremo del servidor del protocolo de
	 * transferencia de ficheros entre pares.
	 * 
	 * @param socket El socket para la comunicación con un cliente que desea
	 *               descargar ficheros.
	 */
	public static void serveFilesToClient(Socket socket) {
		/*
		 * TODO: (Boletín SocketsTCP) Crear dis/dos a partir del socket
		 */		
		/*
		 * TODO: (Boletín SocketsTCP) Mientras el cliente esté conectado, leer mensajes
		 * de socket, convertirlo a un objeto PeerMessage y luego actuar en función del
		 * tipo de mensaje recibido, enviando los correspondientes mensajes de
		 * respuesta.
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Para servir un fichero, hay que localizarlo a
		 * partir de su hash (o subcadena) en nuestra base de datos de ficheros
		 * compartidos. Los ficheros compartidos se pueden obtener con
		 * NanoFiles.db.getFiles(). Los métodos lookupHashSubstring y
		 * lookupFilenameSubstring de la clase FileInfo son útiles para buscar ficheros
		 * coincidentes con una subcadena dada del hash o del nombre del fichero. El
		 * método lookupFilePath() de FileDatabase devuelve la ruta al fichero a partir
		 * de su hash completo.
		 */
		
		try (DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

	        boolean running = true;

			while (running) {
				// Leer el mensaje entrante
	            PeerMessage receivedMsg = PeerMessage.readMessageFromInputStream(dis);
				byte opcode = receivedMsg.getOpcode();
				

				switch (opcode) {
				
	              case PeerMessageOps.OPCODE_DOWNLOAD:
	                    handleDownloadRequest(receivedMsg, dos);
	                    break;

	                case PeerMessageOps.OPCODE_GET_CHUNK:
	                    handleChunkRequest(receivedMsg, dos);
	                    break;

	                case PeerMessageOps.OPCODE_UPLOAD_FILE:
	                    handleFileUpload(receivedMsg, dis);
	                    break;

	                case PeerMessageOps.OPCODE_END_OF_FILE:
	                    System.out.println("[Server] Cliente finalizó la conexión.");
	                    running = false;
	                    break;

				default:
					System.err.println("Opcode desconocido: " + opcode);
					break;

				}
			}

		} catch (IOException e) {
			System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
		}

	}

	private static void handleDownloadRequest(PeerMessage receivedMsg, DataOutputStream dos) throws IOException {
	    String requestedFile = receivedMsg.getFile_name().toString();
	    
	    // Obtener la lista de archivos disponibles
	    FileInfo[] files = NanoFiles.db.getFiles();
	    FileInfo[] matchingFiles = FileInfo.lookupFilenameSubstring(files, requestedFile);

	    if (matchingFiles.length > 0) {
	        // Enviar la lista de archivos encontrados
	        dos.writeByte(PeerMessageOps.OPCODE_FILE);
	        dos.writeInt(matchingFiles.length);
	        
	        for (FileInfo file : matchingFiles) {
	            dos.writeUTF(file.fileName);
	            dos.writeLong(file.fileSize);
	        }
	    } else {
	        dos.writeByte(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
	    }
	}

	
	private static void handleChunkRequest(PeerMessage receivedMsg, DataOutputStream dos) throws IOException {
	    String fileHash = receivedMsg.getHash().toString();
	    long offset = receivedMsg.getOffset();
	    int chunkSize = receivedMsg.getChunkSize();

	    String filePath = NanoFiles.db.lookupFilePath(fileHash);
	    if (filePath == null) {
	        dos.writeByte(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
	        return;
	    }

	    try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
	        file.seek(offset);
	        byte[] buffer = new byte[chunkSize];
	        int bytesRead = file.read(buffer);

	        dos.writeByte(PeerMessageOps.OPCODE_FILE);
	        dos.writeInt(bytesRead);
	        dos.write(buffer, 0, bytesRead);
	    }
	}

	private static void handleFileUpload(PeerMessage receivedMsg, DataInputStream dis) throws IOException {
	    String filename = receivedMsg.getFile_name().toString();
	    long fileSize = receivedMsg.getChunkSize();
	    String savePath = "uploads/" + filename;

	    try (FileOutputStream fos = new FileOutputStream(savePath)) {
	        byte[] buffer = new byte[4096];
	        long bytesReceived = 0;

	        while (bytesReceived < fileSize) {
	            int bytesToRead = (int) Math.min(buffer.length, fileSize - bytesReceived);
	            int bytesRead = dis.read(buffer, 0, bytesToRead);
	            fos.write(buffer, 0, bytesRead);
	            bytesReceived += bytesRead;
	        }
	        System.out.println("[Server] Archivo recibido: " + filename);
	    }
	}


}