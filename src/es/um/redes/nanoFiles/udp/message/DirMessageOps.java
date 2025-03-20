package es.um.redes.nanoFiles.udp.message;

public class DirMessageOps {

	/*
	 * TODO: (Boletín MensajesASCII) Añadir aquí todas las constantes que definen
	 * los diferentes tipos de mensajes del protocolo de comunicación con el
	 * directorio (valores posibles del campo "operation").
	 */
	public static final String OPERATION_INVALID = "invalid_operation";
	public static final String OPERATION_PING = "ping";
	public static final String OPERATION_GETFILELIST = "getFileList";
	public static final String OPERATION_SERVER = "registerFileServer";
	public static final String OPERATION_DOWNLOAD = "download";

	public static final String OPERATION_PING_OK = "pingOk";
	public static final String OPERATION_PING_BAD = "pingBad";

	public static final String OPERATION_GETFILELIST_OK = "getFileListOk";
	public static final String OPERATION_GETFILELIST_BAD = "getFileListBad";

	public static final String OPERATION_SERVER_OK = "registerFileServerOk";
	public static final String OPERATION_SERVER_BAD = "registerFileServerBad";

	public static final String OPERATION_DOWNLOAD_OK = "downloadOk";
	public static final String OPERATION_DOWNLOAD_BAD = "downloadBad";

}
