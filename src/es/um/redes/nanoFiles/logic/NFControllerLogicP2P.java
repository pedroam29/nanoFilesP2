package es.um.redes.nanoFiles.logic;

import java.net.InetSocketAddress;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.application.NanoFiles;



import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.util.FileDigest;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicP2P {
	/*
	 * TODO: Se necesita un atributo NFServer que actuará como servidor de ficheros
	 * de este peer
	 */
	private NFServer fileServer = null;

	private NFControllerLogicDir controllerDir;


	protected NFControllerLogicP2P() {
	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean startFileServer() {
		boolean serverRunning = false;
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		if (fileServer != null) {
			System.err.println("[NFControllerLogicP2P]File server is already running");
		} else {

			// 2. Crear y lanzar un hilo para el servidor en segundo plano
			Thread serverThread = new Thread(() -> {
			    try {
			        fileServer = new NFServer(); // Instanciar el servidor
			        fileServer.startServer(); // Iniciar el servidor (debe contener el ServerSocket)
			    } catch (IOException e) {
			        System.err.println("Error al iniciar el servidor: " + e.getMessage());
			    }
			});

			serverThread.setDaemon(true); // Hacer que el hilo del servidor termine cuando el programa finaliza
			serverThread.start(); // Iniciar el hilo


			// Esperar hasta que el servidor haya sido inicializado
			int retries = 10;
			while (fileServer == null && retries > 0) {
			    try {
			        Thread.sleep(100); // Esperar 100 ms
			    } catch (InterruptedException e) {
			        e.printStackTrace();
			    }
			    retries--;
			}

			if (fileServer != null) {
			    int port = fileServer.getPort();
			    if (port > 0) {
			        System.out.println("Servidor iniciado en el puerto: " + port);
			        serverRunning = true;
			    } else {
			        System.err.println("Error: puerto no válido.");
			    }
			} else {
			    System.err.println("Error: el servidor no se inició correctamente.");
			}



		}
		return serverRunning;

	}

	protected void testTCPServer() {
		assert (NanoFiles.testModeTCP);
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		assert (fileServer == null);
		try {

			fileServer = new NFServer();
			/*
			 * (Boletín SocketsTCP) Inicialmente, se creará un NFServer y se ejecutará su
			 * método "test" (servidor minimalista en primer plano, que sólo puede atender a
			 * un cliente conectado). Posteriormente, se desactivará "testModeTCP" para
			 * implementar un servidor en segundo plano, que se ejecute en un hilo
			 * secundario para permitir que este hilo (principal) siga procesando comandos
			 * introducidos mediante el shell.
			 */
			fileServer.test();
			// Este código es inalcanzable: el método 'test' nunca retorna...
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("[NFControllerLogicP2P]Cannot start the file server");
			fileServer = null;
		}
	}

	public void testTCPClient() {

		assert (NanoFiles.testModeTCP);
		/*
		 * (Boletín SocketsTCP) Inicialmente, se creará un NFConnector (cliente TCP)
		 * para conectarse a un servidor que esté escuchando en la misma máquina y un
		 * puerto fijo. Después, se ejecutará el método "test" para comprobar la
		 * comunicación mediante el socket TCP. Posteriormente, se desactivará
		 * "testModeTCP" para implementar la descarga de un fichero desde múltiples
		 * servidores.
		 */

		try {
			NFConnector nfConnector = new NFConnector(new InetSocketAddress(NFServer.PORT));
			nfConnector.test();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList       La lista de direcciones de los servidores a
	 *                                los que se conectará
	 * @param targetFileNameSubstring Subcadena del nombre del fichero a descargar
	 * @param localFileName           Nombre con el que se guardará el fichero
	 *                                descargado
	 */
	protected boolean downloadFileFromServers(InetSocketAddress[] serverAddressList, String targetFileNameSubstring,
			String localFileName) {
		boolean downloaded = false;

		if (serverAddressList.length == 0) {
			System.err.println("* Cannot start download - No list of server addresses provided");
			return false;
		}
		/*
		 * TODO: Crear un objeto NFConnector distinto para establecer una conexión TCP
		 * con cada servidor de ficheros proporcionado, y usar dicho objeto para
		 * descargar trozos (chunks) del fichero. Se debe comprobar previamente si ya
		 * existe un fichero con el mismo nombre (localFileName) en esta máquina, en
		 * cuyo caso se informa y no se realiza la descarga. Se debe asegurar que el
		 * fichero cuyos datos se solicitan es el mismo para todos los servidores
		 * involucrados (el fichero está identificado por su hash). Una vez descargado,
		 * se debe comprobar la integridad del mismo calculando el hash mediante
		 * FileDigest.computeFileChecksumString. Si todo va bien, imprimir resumen de la
		 * descarga informando de los trozos obtenidos de cada servidor involucrado. Las
		 * excepciones que puedan lanzarse deben ser capturadas y tratadas en este
		 * método. Si se produce una excepción de entrada/salida (error del que no es
		 * posible recuperarse), se debe informar sin abortar el programa
		 */
		
		NFConnector srvConnection;
		// 1. Pedimos al directorio el hash del archivo real que queremos descargar
	 	FileInfo[] files = NanoFiles.db.getFiles();
	    FileInfo[] matchingFiles = FileInfo.lookupFilenameSubstring(files, targetFileNameSubstring);

	    String targetFileHash = null;
	    if (matchingFiles.length > 0) {
	        targetFileHash = matchingFiles[0].fileHash;  // Obtiene el hash del primer archivo encontrado
	    } else {
	        System.err.println("Archivo no encontrado");
	        return false;
	    }


	    // 2. Verificar si el archivo ya existe
	    File localFile = new File(NanoFiles.sharedDirname, localFileName);
	    if (localFile.exists()) {
	        System.err.println("[Download] File already exists: " + localFileName);
	        return false;
	    }

	    // 3. Intentar crear el archivo
	    try {
	        if (!localFile.createNewFile()) {
	            System.err.println("[Download] Error creating local file.");
	            return false;
	        }
	    } catch (IOException e) {
	        System.err.println("[Download] Failed to create local file: " + e.getMessage());
	        return false;
	    }

	    // 4. Descargar desde los servidores en orden
	    int n = 1;
	    int totalServers = serverAddressList.length;
	    try (FileOutputStream fos = new FileOutputStream(localFile)) {
	        for (InetSocketAddress server : serverAddressList) {
	            try {
	            	srvConnection = new NFConnector(server);
	                System.out.println("[Download] Connecting to: " + server);
	                
	                // Descargar un chunk del archivo
	                boolean chunkDownloaded = srvConnection.downloadFileChunk(targetFileHash, localFile, n, totalServers);
	                if (chunkDownloaded) {
	                    System.out.println("[Download] Chunk " + n + " downloaded from " + server);
	                    downloaded = true;
	                } else {
	                    System.err.println("[Download] Failed to download chunk " + n + " from " + server);
	                }
	            } catch (IOException e) {
	                System.err.println("[Download] Connection error with " + server + ": " + e.getMessage());
	            }
	            n++;
	        }
	    } catch (IOException e) {
	        System.err.println("[Download] File write error: " + e.getMessage());
	        return false;
	    }

	    // 5. Verificar integridad del archivo descargado
	    if (downloaded) {
	        String downloadedFileHash = FileDigest.computeFileChecksumString(localFile.toString());
	        if (!downloadedFileHash.equals(targetFileHash)) {
	            System.err.println("[Download] File integrity check failed! Expected hash: " + targetFileHash +
	                               " but got: " + downloadedFileHash);
	            localFile.delete();  // Borrar archivo corrupto
	            return false;
	        }
	        System.out.println("[Download] File successfully downloaded and verified.");
	    } else {
	        System.err.println("[Download] Download failed from all servers.");
	        localFile.delete();
	        return false;
	    }

	    return downloaded;
	}

	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	protected int getServerPort() {
		return fileServer.getPort();
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	protected void stopFileServer() {
		/*
		 * TODO: Enviar señal para detener nuestro servidor de ficheros en segundo plano
		 */
		this.fileServer.stopserver();
		this.fileServer = null;


	}

	protected boolean serving() {
		boolean result = false;



		return result;

	}

	protected boolean uploadFileToServer(FileInfo matchingFile, String uploadToServer) {
		boolean result = false;



		return result;
	}

}
