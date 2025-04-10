package es.um.redes.nanoFiles.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.udp.server.NFDirectoryServer;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	/**
	 * Puerto en el que atienden los servidores de directorio
	 */
	private static final int DIRECTORY_PORT = 6868;
	/**
	 * Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
	 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
	 * recuperar el control
	 */
	private static final int TIMEOUT = 1000;
	/**
	 * Número de intentos máximos para obtener del directorio una respuesta a una
	 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
	 * cuenta como un intento.
	 */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;

	/**
	 * Socket UDP usado para la comunicación con el directorio
	 */
	private DatagramSocket socket;
	/**
	 * Dirección de socket del directorio (IP:puertoUDP)
	 */
	private InetSocketAddress directoryAddress;
	/**
	 * Nombre/IP del host donde se ejecuta el directorio
	 */
	private String directoryHostname;

	public DirectoryConnector(String hostname) throws IOException {
		// Guardamos el string con el nombre/IP del host
		directoryHostname = hostname;
		/*
		 * TODO: (Boletín SocketsUDP) Convertir el string 'hostname' a InetAddress y
		 * guardar la dirección de socket (address:DIRECTORY_PORT) del directorio en el
		 * atributo directoryAddress, para poder enviar datagramas a dicho destino.
		 */
		InetAddress directoryIp = InetAddress.getByName(hostname);
		directoryAddress = new InetSocketAddress(directoryIp, DIRECTORY_PORT);
		/*
		 * 
		 * TODO: (Boletín SocketsUDP) Crea el socket UDP en cualquier puerto para enviar
		 * datagramas al directorio
		 */
		socket = new DatagramSocket();
		socket.setSoTimeout(TIMEOUT);

	}

	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 */
	private byte[] sendAndReceiveDatagrams(byte[] requestData) {
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		byte response[] = null;

		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.exit(-1);
		}

		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.exit(-1);
		}

		DatagramPacket sendPacket = new DatagramPacket(requestData, requestData.length, directoryAddress);
		Arrays.fill(responseData, (byte) 0);
		DatagramPacket receivePacket = new DatagramPacket(responseData, responseData.length);

		int attempts = 0;
		boolean receivedResponse = false;

		try {
			socket.setSoTimeout(TIMEOUT); // Configurar timeout para la recepción
			while (attempts < MAX_NUMBER_OF_ATTEMPTS && !receivedResponse) {
				try {
					socket.send(sendPacket); // Enviar datagrama
					socket.receive(receivePacket); // Intentar recibir respuesta

					receivedResponse = true;
				} catch (SocketTimeoutException e) {
					attempts++;
					System.err.println(
							"Timeout alcanzado. Reintentando... (" + attempts + "/" + MAX_NUMBER_OF_ATTEMPTS + ")");
				}
			}

			if (receivedResponse) {
				response = new byte[receivePacket.getLength()];
				System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), response, 0,
						receivePacket.getLength());
			} else {
				System.err.println("No se recibió respuesta tras " + MAX_NUMBER_OF_ATTEMPTS + " intentos.");
			}

		} catch (IOException e) {
			System.err.println("Error de E/S en sendAndReceiveDatagrams: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}

		if (response != null && response.length == responseData.length) {
			System.err.println("Your response is as large as the datagram reception buffer!!\n"
					+ "You must extract from the buffer only the bytes that belong to the datagram!");
		}

		return response;
	}

	/**
	 * Método para probar la comunicación con el directorio mediante el envío y
	 * recepción de mensajes sin formatear ("en crudo")
	 * 
	 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
	 */
	public boolean testSendAndReceive() {
		/*
		 * TODO: (Boletín SocketsUDP) Probar el correcto funcionamiento de
		 * sendAndReceiveDatagrams. Se debe enviar un datagrama con la cadena "ping" y
		 * comprobar que la respuesta recibida empieza por "pingok". En tal caso,
		 * devuelve verdadero, falso si la respuesta no contiene los datos esperados.
		 */
		boolean success = false;
		try {
			byte[] requestData = "ping".getBytes();

			byte[] responseData = sendAndReceiveDatagrams(requestData);

			if (responseData != null) {
				String responseString = new String(responseData).trim();
				if (responseString.startsWith("pingok")) {
					success = true;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in testSendAndReceive: " + e.getMessage());
		}

		return success;
	}

	public String getDirectoryHostname() {
		return directoryHostname;
	}

	/**
	 * Método para "hacer ping" al directorio, comprobar que está operativo y que
	 * usa un protocolo compatible. Este método no usa mensajes bien formados.
	 * 
	 * @return Verdadero si
	 */
	public boolean pingDirectoryRaw() {
		boolean success = false;
		/*
		 * TODO: (Boletín EstructuraNanoFiles) Basándose en el código de
		 * "testSendAndReceive", contactar con el directorio, enviándole nuestro
		 * PROTOCOL_ID (ver clase NanoFiles). Se deben usar mensajes "en crudo" (sin un
		 * formato bien definido) para la comunicación.
		 * 
		 * PASOS: 1.Crear el mensaje a enviar (String "ping&protocolId"). 2.Crear un
		 * datagrama con los bytes en que se codifica la cadena : 4.Enviar datagrama y
		 * recibir una respuesta (sendAndReceiveDatagrams). : 5. Comprobar si la cadena
		 * recibida en el datagrama de respuesta es "welcome", imprimir si éxito o
		 * fracaso. 6.Devolver éxito/fracaso de la operación.
		 */

		try {
			byte[] requestData = new String("ping&" + NanoFiles.PROTOCOL_ID).getBytes(StandardCharsets.UTF_8);
			byte[] response = sendAndReceiveDatagrams(requestData);
			if (response != null) {
				String receivedMessage = new String(response, StandardCharsets.UTF_8).trim();
				if (receivedMessage.equals("welcome")) {
					success = true;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in pingDirectoryRaw: " + e.getMessage());
		}

		return success;
	}

	/**
	 * Método para "hacer ping" al directorio, comprobar que está operativo y que es
	 * compatible.
	 * 
	 * @return Verdadero si el directorio está operativo y es compatible
	 */
	public boolean pingDirectory() {
		boolean success = false;
		/*
		 * TODO: (Boletín MensajesASCII) Hacer ping al directorio 1.Crear el mensaje a
		 * enviar (objeto DirMessage) con atributos adecuados (operation, etc.) NOTA:
		 * Usar como operaciones las constantes definidas en la clase DirMessageOps :
		 * 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		 * 3.Crear un datagrama con los bytes en que se codifica la cadena : 4.Enviar
		 * datagrama y recibir una respuesta (sendAndReceiveDatagrams). : 5.Convertir
		 * respuesta recibida en un objeto DirMessage (método DirMessage.fromString)
		 * 6.Extraer datos del objeto DirMessage y procesarlos 7.Devolver éxito/fracaso
		 * de la operación
		 */
		DirMessage pingMessage = new DirMessage(DirMessageOps.OPERATION_PING, NanoFiles.PROTOCOL_ID);
		String pingMessageString = pingMessage.toString();
		byte[] requestData = pingMessageString.getBytes();
		byte[] response = sendAndReceiveDatagrams(requestData);
		if (response != null) {
			String responseAString = new String(response, 0, response.length);
			System.out.println("Receiving..." + responseAString);

			DirMessage msgFromServer = DirMessage.fromString(responseAString);
			if (msgFromServer != null && msgFromServer.getOperation().equals(DirMessageOps.OPERATION_PING_OK)) {
				success = true;
			}
		}

		return success;
	}

	/**
	 * Método para dar de alta como servidor de ficheros en el puerto indicado y
	 * publicar los ficheros que este peer servidor está sirviendo.
	 * 
	 * @param serverPort El puerto TCP en el que este peer sirve ficheros a otros
	 * @param files      La lista de ficheros que este peer está sirviendo.
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y acepta la lista de ficheros, falso en caso contrario.
	 */
	public boolean registerFileServer(int serverPort, FileInfo[] files) {
		boolean success = false;

		// TODO: Ver TODOs en pingDirectory y seguir esquema similar
		DirMessage msgToSend = new DirMessage(DirMessageOps.OPERATION_SERVE, NanoFiles.PROTOCOL_ID, serverPort, files);
		String msgToSendString = msgToSend.toString();
		byte[] requestData = msgToSendString.getBytes();
		byte[] response = sendAndReceiveDatagrams(requestData);

		if (response != null) {

			String responseAString = new String(response, 0, response.length);
			System.out.println("Receiving..." + responseAString);
			DirMessage msgFromServer = DirMessage.fromString(responseAString);

			if (msgFromServer != null && msgFromServer.getOperation().equals(DirMessageOps.OPERATION_SERVE_RESPONSE)) {

				success = true;

			}

		}

		return success;
	}

	/**
	 * Método para obtener la lista de ficheros que los peers servidores han
	 * publicado al directorio. Para cada fichero se debe obtener un objeto FileInfo
	 * con nombre, tamaño y hash. Opcionalmente, puede incluirse para cada fichero,
	 * su lista de peers servidores que lo están compartiendo.
	 * 
	 * @return Los ficheros publicados al directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 */
	public FileInfo[] getFileList() {
		FileInfo[] filelist = new FileInfo[0];
		// TODO: Ver TODOs en pingDirectory y seguir esquema similar

		DirMessage getFileListMessage = new DirMessage(DirMessageOps.OPERATION_FILELIST, NanoFiles.PROTOCOL_ID);
		String getFileListMessageString = getFileListMessage.toString();
		byte[] requestData = getFileListMessageString.getBytes();
		byte[] response = sendAndReceiveDatagrams(requestData);

		if (response != null) {

			String responseAString = new String(response, 0, response.length);
			DirMessage msgFromServer = DirMessage.fromString(responseAString);

			if (msgFromServer != null && msgFromServer.getOperation().equals(DirMessageOps.OPERATION_FILELIST_RESPONSE)) {
				// Añadir elementos a filelist;
				filelist = msgFromServer.getFiles();
			}

		}

		return filelist;
	}

	/**
	 * Método para obtener la lista de servidores que tienen un fichero cuyo nombre
	 * contenga la subcadena dada.
	 * 
	 * @filenameSubstring Subcadena del nombre del fichero a buscar
	 * 
	 * @return La lista de direcciones de los servidores que han publicado al
	 *         directorio el fichero indicado. Si no hay ningún servidor, devuelve
	 *         una lista vacía.
	 */
	public InetSocketAddress[] getServersSharingThisFile(String filenameSubstring) {
		// TODO: Ver TODOs en pingDirectory y seguir esquema similar
		InetSocketAddress[] serversList = new InetSocketAddress[0];

		DirMessage getFileListMessage = new DirMessage(DirMessageOps.OPERATION_DOWNLOAD, NanoFiles.PROTOCOL_ID,
				filenameSubstring);
		String getFileListMessageString = getFileListMessage.toString();
		System.out.println("[Client] Sending request: " + getFileListMessage.toString());
		byte[] requestData = getFileListMessageString.getBytes();
		byte[] response = sendAndReceiveDatagrams(requestData);

		if (response != null) {

			String responseAString = new String(response, 0, response.length);
			System.out.println("Receiving..." + responseAString);
			DirMessage msgFromServer = DirMessage.fromString(responseAString);

			if (msgFromServer != null && msgFromServer.getOperation().equals(DirMessageOps.OPERATION_DOWNLOAD_OK)) {

				// Añadir elementos a serverList;
				serversList = msgFromServer.getServerList();
			}

		}
		System.out.println("[Client] Lista de servidores recibida: " + Arrays.toString(serversList));
		return serversList;
	}

	/**
	 * Método para darse de baja como servidor de ficheros.
	 * 
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y ha dado de baja sus ficheros.
	 */
	public boolean unregisterFileServer() {
		boolean success = false;

		return success;
	}

}
