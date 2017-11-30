package server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import client.ClientInterface.InsecureHostnameVerifier;
import homolib.*;
import resources.Entry;
import resources.KeySaver;


/**
 * Implementacao do servidor em REST 
 */
@Path("/entries")
public class ProxyResources {

	public String serverUri;


	private static Client client;
	private static URI serverURI;
	private static WebTarget target;
	private PaillierKey pk;
	//	private KeySaver ks;
	//	private KeyPair keyPair;
	//	private RSAPublicKey publicKey;
	//	private RSAPrivateKey privateKey;
	//	private String pkSer;


	public ProxyResources(String serverUri) {
		this.serverUri = serverUri;

		//Server connection
		serverURI = UriBuilder.fromUri(serverUri).port(11100).build();
		client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		target = client.target( serverURI );


		//Homomorphic Init
		//		pk = HomoAdd.generateKey();
		//		keyPair = HomoMult.generateKey();
		//		publicKey = (RSAPublicKey) keyPair.getPublic();
		//		privateKey = (RSAPrivateKey) keyPair.getPrivate();
		//		
		//		pkSer = HelpSerial.toString(pk.getNsquare());

		keysInit();
	}

	@POST
	@Path("/ps")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putSet( Entry entry) {
		System.out.println("Received Put Set Request");
		putSetImplementation(entry.getkey(), entry.getAttributes());

	}


	@GET
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public byte[] getSet(@PathParam("id") String id){
		System.out.println("Received Get Set Request");
		return getSetImplementation(id);
	}


	@POST
	@Path("/adde/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addElement( @PathParam("id") String id) {
		System.out.println("Received Add Element Request");
		return addElementImplementation(id);
	}

	@GET
	@Path("/{id}/{pos}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String readElement(@PathParam("id") String id, @PathParam("pos") int pos) {
		System.out.println("Received Read Element Request");

		byte[] res = readElementImplementation(id,pos);
		String a = new String(res, StandardCharsets.UTF_8);
		return a;
	}


	@GET
	@Path("/ie/{id}/{element}")
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean isElement(@PathParam("id") String id, @PathParam("element") String element) {
		System.out.println("Received Is Element Request");
		return isElementImplementation(id,element);
	}

	@PUT
	@Path("/{id}/{pos}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response writeElement(@PathParam("id") String id, @PathParam("pos") int pos, String new_element) {
		System.out.println("Received Write Element Request");
		return writeElementImplementation(id, pos, new_element);
	}

	@DELETE
	@Path("/rs/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removeSet(@PathParam("id") String id){
		System.out.println("Received Remove Set Request");
		return removeSetImplementation(id);
	}

	@GET
	@Path("/sum/{id1}/{id2}/{pos}")
	@Produces(MediaType.APPLICATION_JSON)
	public byte[] sum(@PathParam("id1") String id1, @PathParam("id2") String id2, @PathParam("pos") int pos) {
		System.out.println("Received Sum Request");
		return sumImplementation(id1, id2, pos);

	}

	@GET
	@Path("/mult/{id1}/{id2}/{pos}")
	@Produces(MediaType.APPLICATION_JSON)
	public byte[] mult(@PathParam("id1") String id1, @PathParam("id2") String id2, @PathParam("pos") int pos) {
		System.out.println("Received Mult Request");
		return multImplementation(id1, id2, pos);
	}


	public byte[] sumImplementation(String key1, String key2, int pos) {

		byte[] response = target.path("/entries/sum/"+key1+"/"+key2+"/"+pos)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<byte[]>(){});

		ByteArrayInputStream in = new ByteArrayInputStream(response);
		DataInputStream res = new DataInputStream(in);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);

		byte[] result = null;
		try {
			int size = res.readInt();
			result = new byte[size];

			res.read(result, 0, size);

			BigInteger resultDec = homoSumDecryption(result);
			
			System.out.println(resultDec.intValue());
			
			byte[] valueRead = resultDec.toByteArray();
//			if (valueRead[0] == 0) {
//				byte[] tmp = new byte[valueRead.length - 1];
//				System.arraycopy(valueRead, 1, tmp, 0, tmp.length);
//				valueRead = tmp;
//			}

			System.out.println(new String(valueRead, StandardCharsets.UTF_8));
			dos.writeUTF(new String(valueRead, StandardCharsets.UTF_8));

		} catch (Exception e) {
			e.printStackTrace();
		}

		if(response==null)
			System.out.println("decription nao deu");

		return out.toByteArray();

	}

	public byte[] multImplementation(String key1, String key2, int pos) {

		//		BigInteger modulus = publicKey.getModulus();
		//		//String modString = HelpSerial.toString(modulus);
		//		BigInteger pubExp = publicKey.getPublicExponent();
		//		//String expString = HelpSerial.toString(pubExp);
		byte[] response = target.path("/entries/mult/"+key1+"/"+key2+"/"+pos+"/")
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<byte[]>(){});
		//
		//		ByteArrayInputStream in = new ByteArrayInputStream(response);
		//		DataInputStream res = new DataInputStream(in);
		//		
		//		ByteArrayOutputStream out = new ByteArrayOutputStream();
		//		DataOutputStream dos = new DataOutputStream(out);
		//		
		//		byte[] result = null;
		//		byte[] resultDecrypted = null;
		//		try {
		//			int size = res.readInt();
		//			result = new byte[size];
		//			
		//			res.read(result, 0, size);
		//			
		//			if (result[0] == 0) {
		//				System.out.println("Entri");
		//			    byte[] tmp = new byte[result.length - 1];
		//			    System.arraycopy(result, 1, tmp, 0, tmp.length);
		//			    result = tmp;
		//			}
		//			
		//			BigInteger resultDec = homoMultDecryption(result);
		//			resultDecrypted = resultDec.toByteArray();
		//			
		//			
		//			
		//			dos.writeUTF(new String(resultDecrypted));
		//			System.out.println("RES: " + resultDec.toString());
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		
		//		if(response==null)
		//			System.out.println("decription nao deu");

		return response;

	}

	public Response addElementImplementation(String id) {
		return null;
	}

	public Response writeElementImplementation(String key, int pos, String element) {
		return null;
	}

	public boolean isElementImplementation(Object key, Object element) {
		return false;
	}

	public Response removeSetImplementation(Object key) {
		return null;
	}

	public byte[] readElementImplementation(Object key, int pos) {
		return null;
	}

	public byte[] getSetImplementation(String key) {

		byte[] response = target.path("/entries/"+key)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<byte[]>() {});

		ByteArrayInputStream in = new ByteArrayInputStream(response);
		DataInputStream res = new DataInputStream(in);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);

		int attSize;
		try {
			attSize = res.readInt();
			dos.writeInt(attSize);

			for(int i = 0; i < attSize; i++) {
				int keySize = res.readInt();
				byte[] keyRead = new byte[keySize];
				res.read(keyRead, 0, keySize);

				int valueSize = res.readInt();
				byte[] valueRead = new byte[valueSize];
				res.read(valueRead, 0, valueSize);

				String keyString = new String(keyRead, StandardCharsets.UTF_8);

				if(keyString.equals("idade")) {

					BigInteger ageBigInt = homoSumDecryption(valueRead);

					valueRead = ageBigInt.toByteArray();
					if (valueRead[0] == 0) {
						byte[] tmp = new byte[valueRead.length - 1];
						System.arraycopy(valueRead, 1, tmp, 0, tmp.length);
						valueRead = tmp;
					}

				}
				String k = new String(keyRead, StandardCharsets.UTF_8);
				String v = new String(valueRead, StandardCharsets.UTF_8);

				dos.writeUTF(k);
				dos.writeUTF(v);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return out.toByteArray();
	}


	public Response putSetImplementation(String id, Map<String, byte[]> attributes) {

		Entry entry = new Entry(id, attributes);

		Map<String, byte[]> hm = entry.getAttributes();

		byte[] age = hm.get("idade");

		byte[] ageEncrypted = homoSumEncryption(age);

		hm.remove("idade");
		hm.put("idade", ageEncrypted);
		entry.setAttributes(hm);


		//		byte[] salary = hm.get("salary");
		//		
		//		byte[] salaryEncrypted = homoMultEncryption(salary);
		//		
		//		hm.remove("salary");
		//		hm.put("salary", salaryEncrypted);
		//		entry.setAttributes(hm);

		Response response = target.path("/entries/ps/")
				.request()
				.post( Entity.entity(entry, MediaType.APPLICATION_JSON));


		return response;
	}

	public String homoSearch(String name) {

		SecretKey key = homolib.HomoSearch.generateKey();

		return HomoSearch.encrypt(key, name);

	}

	public byte[] homoSumEncryption(byte[] n1) {

		BigInteger big1 = new BigInteger(n1);

		try {
			return HomoAdd.encrypt(big1, pk).toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("> Erro Sum encryption!");
		}
		return null;

	}

	public BigInteger homoSumDecryption(byte[] n1) {

		BigInteger big1 = new BigInteger(n1);

		try {
			return HomoAdd.decrypt(big1, pk);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("> Erro Sum decryption!");
		}

		//se for necessario retornar -1 ou assim
		return new BigInteger("0");
	}

	public byte[] homoMultEncryption(byte[] n1) {

		BigInteger big1 = new BigInteger(n1);

		//		try {
		//			return HomoMult.encrypt(publicKey, big1).toByteArray();
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//			System.out.println("> Erro Sum encryption!");
		//		}
		return null;

	}

	public BigInteger homoMultDecryption(byte[] n1) {

		BigInteger big1 = new BigInteger(n1);

		//		try {
		//			return HomoMult.decrypt(privateKey, big1);
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//			System.out.println("> Erro Sum decryption!");
		//		}

		//se for necessario retornar -1 ou assim
		return new BigInteger("0");
	}

	public void keysInit() {
		BigInteger p = new BigInteger("138994875545812951942834382474439690820435130839932490341999415525940925897641804006982058331728305518028094046976082044739021915739134929609713303822441456078072581110120254282102952676784311924129620936559571914774806545907957737247353761315125927913016866592166819360121783566808978691030254416533789726693");
		BigInteger q= new BigInteger("152637975394189390753047515830973182510050897925608753126328622014011388321496978820079885386768420397661677894439851061907820324957299354405000725484191709621214388392134576842723993255383837194413281225165643503827653867292977543994297144366482480883834798863184374068696787601934670035614886437268250913383");
		BigInteger lambda = new BigInteger("10607948196740107011263113112364444982670934808724965746964957854658741495667640039265317334625518262598198178365928944533071912489541021242415255953476321425856484799651444087266593665867414997869645455068720501839248538325367051743897075697774539848295736914642573718870483758257254940711467891792974412728881184464299897283156038456606831257596275481472142523889490701184980826833935140650703250267088759774104082577909814268773519407470375344492358922195676586058193827986766377739649187810876738542604957443589423494993950281410265254249262640478495863167011065342670989602324759397634223601938126527528572696172");
		BigInteger n = new BigInteger("21215896393480214022526226224728889965341869617449931493929915709317482991335280078530634669251036525196396356731857889066143824979082042484830511906952642851712969599302888174533187331734829995739290910137441003678497076650734103487794151395549079696591473829285147437740967516514509881422935783585948825458054001779539796909007958811519075388523036991709826291247309439909913967887009064128468444252674245463897937097235561644193881055637184972999431873697986337815674625475787586604125321553921626203752817048904062408590360976021465789740176186638600135130873796140693172633468089964012095930521393908859186032419");
		BigInteger nsquare = new BigInteger("450114259778886752345994972925686146945175719516761522713198032574212830403533700183623261468021445078621760277779263141968015314983806128821058275802755545379938157291541399835009995569683895334890113014285060082790243552087685381383203593754939202348592721888034967877276307286156255678855763399447355959224711916549581546885085076426020478521602260395624180860696575344834409471583415670097862455826930115321719679903616216507036425955946871540061083041188261888011387921155914785756600965822823820335396455565877600925391487234095866282893990501955926369072534595727702891613849272847282083068819208706653784963946902373565121574954362690827541488261200963131888214781747205025305288855145274621870061838362489615190239392460517201952524600831191354242747305690851938059864987581083260076537564172705633914487255514251288518489875766184202330155222520855879090477482527774846732519751497906986646845790885449414100375356594455653634608031360028117692359146525986187077013576268060089986639104363108940356956657085556867213560550690477282013323735081036608396242294777772679166416552518895073108745808291336395838454225511647635991804381856099567271039293748901550303296912200005568012909142450882237697100362114845207902918991561");
		BigInteger g = new BigInteger("19950293663116763090453990435614496656429546004936750643713681638839628984360802254690753073457642597890491419474229124630725270436858099922871064228428869188405759193711584474225392508078761072584969262209203591392977697062778367560140462104969259220360434513546386336111482888518906879285308973005209589687156106121261632395290836627158967423196635906347978865349136484106042024926825515416540252023596523572972106268397307123628372307556455873298535314670201654003329321023638383938038498758458813817813535202493565750369776339278207896999551407066217940601981870998458063724765918043629975451653435644852021163859357958824602097562904923739248348026863232836371316914436694433896104585526583618889543589260766649436539167352513199915070629209562502377179760186675101060425297538244178480893342773055626881053047629755718206511424547009238208171093627045888974864525695780538174238834111557117105384197661317535862107739320573023596178658993266497670163567105733242149815579794740560882249880562508875143663806732041268498799507545748044111608642894133517998981611216785132148822760723784362833084822621395253027986723266735431484518591759328156389589702408961687879283985523092213684998351003072469791541755489658224667218411733");
		BigInteger mu= new BigInteger("10764710170199705233505124648403165529212121097251541238630848444075093447516964785205597958481049283727512957031604667816674248857448556510271529970874283604552567490353946909078236103554182238191052855718220882052619345516515693973054537482264701461696815779529446468287302168827771150173417514736422669687979284523815191260980144040912580877240622591274236027834960423283507192909636457092402464733915052269697941390794296895075429564446858419232956003307010864348252281569967299423436088375589035087840328077636794366867752894760877219307000371333455584163863445072399914002612110509695415230312776539225702051571");

		pk = new PaillierKey(p, q, lambda, n, nsquare, g, mu);

		//		pk.printValues();
	}

}

