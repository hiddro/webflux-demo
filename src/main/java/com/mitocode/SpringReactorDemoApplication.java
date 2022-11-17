package com.mitocode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class SpringReactorDemoApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringReactorDemoApplication.class);

	private static List<String> dishes = new ArrayList<>();

	public void createMono(){
		Mono<String> m1 = Mono.just("Hello World");
		//subscribir = puedo saber que pasa con lo que me ha devuelto
		m1.subscribe(x -> log.info("Data: " + x));

		Mono.just(5).subscribe(x -> log.info("Data: " + x));
	}

	public void createFlux(){
		Flux<String> fl1 = Flux.fromIterable(dishes);

//		fl1.subscribe(x -> log.info("Data: " + x));

		//transformando de flux a mono
		fl1.collectList().subscribe(x -> log.info("Data: " + x));
	}

	//sirve mas para depuracion y saber que pasa internamente
	public void m1doOnNext(){
		Flux<String> fx1 = Flux.fromIterable(dishes);

		fx1.doOnNext(e -> log.info(e))
				.subscribe();
	}

	public void m2map(){
		Flux<String> fx1 = Flux.fromIterable(dishes);

		fx1.map(e -> e.toUpperCase())
				.subscribe(log::info);
	}

	public void m3flatMap(){
		Mono.just("jaime").map(x -> 31).subscribe(x -> log.info("Data: " + x)); // 31
		Mono.just("jaime").map(x -> Mono.just(31)).subscribe(x -> log.info("Data: " + x)); // me trae un objecto
		Mono.just("jaime").flatMap(x -> Mono.just(31)).subscribe(x -> log.info("Data: " + x));
	}

	public void m4range(){
		Flux<Integer> fx1 = Flux.range(0, 10);
		fx1.map(x -> x + 1).subscribe(x -> log.info(x.toString()));
	}

	public void m5delayElements() throws InterruptedException {
		Flux.range(0, 10)
				.delayElements(Duration.ofSeconds(2))
				.doOnNext(x -> log.info("Data: " + x))
				.subscribe();

		Thread.sleep(22000);
	}

	public void m6zipWith(){
		List<String> clients = new ArrayList<>();
		clients.add("Client 1");
		clients.add("Client 2");
//		clients.add("Client 3");

		Flux<String> fx1 = Flux.fromIterable(dishes);
		Flux<String> fx2 = Flux.fromIterable(clients);

		fx1.zipWith(fx2, (d, c) -> d + "-" + c)
				.subscribe(log::info);
	}

	public void m7merge() {
		List<String> clients = new ArrayList<>();
		clients.add("Client 1");
		clients.add("Client 2");
//		clients.add("Client 3");

		Flux<String> fx1 = Flux.fromIterable(dishes);
		Flux<String> fx2 = Flux.fromIterable(clients);

		Flux.merge(fx1, fx2).subscribe(log::info);
	}

	public void m8Filter(){
		Flux<String> fx1 = Flux.fromIterable(dishes);
		fx1.filter(d -> d.startsWith("Ce"))
				.subscribe(log::info);
	}

	public void m9takeLast(){
		Flux<String> fx1 = Flux.fromIterable(dishes);
		fx1.takeLast(2).subscribe(log::info); // 1 obtiene el ultimo // 2 los dos ultimos
	}

	public void m10take(){
		Flux<String> fx1 = Flux.fromIterable(dishes);
		fx1.take(1).subscribe(log::info); // es el inicio
	}

	public void m11DefaultifEmpty(){
		dishes = new ArrayList<>();
		Flux<String> fx1 = Flux.fromIterable(dishes);

		fx1.map(e -> "P: " + e)
				.defaultIfEmpty("Empty Flux")
				.subscribe(log::info);
	}

	public void m12Error(){
		Flux<String> fx1 = Flux.fromIterable(dishes);

		fx1.doOnNext(d -> {
			throw new ArithmeticException("BAD NUMBER");
		})
//				.onErrorReturn("error, please rebbot your system")
				.onErrorMap(ex -> new Exception(ex.getMessage())) // controlas las excepciones
				.subscribe(log::info);
	}

	public void m13Threads(){
		final Mono<String> mono = Mono.just("hello ");

		Thread t = new Thread(() -> mono
				.map(msg -> msg + "thread ")
				.subscribe(v ->
						System.out.println(v + Thread.currentThread().getName())
				)
		);
		System.out.println(Thread.currentThread().getName());
		t.start();
	}

	public void m14PublishOn(){
		Flux.range(1, 2)
				.map(x -> {
					log.info("valor: " + x + " | thread: " + Thread.currentThread().getName());
					return x;
				})
				.publishOn(Schedulers.single()) // solo afecta a operadores que estan debajo de el
				.map(x -> {
					log.info("valor: " + x + " | thread: " + Thread.currentThread().getName());
					return x;
				})
				.publishOn(Schedulers.boundedElastic())
				.map(x -> {
					log.info("valor: " + x + " | thread: " + Thread.currentThread().getName());
					return x;
				})
				.subscribe();
	}

	public void m15SubscribeOn() throws InterruptedException{
		Flux.range(1, 2)
				.subscribeOn(Schedulers.boundedElastic()) // solo hara caso al primmer subscribeon que encuentra
				.map(x -> {
					log.info("valor: " + x + " | thread: " + Thread.currentThread().getName());
					return x;
				})
				.subscribeOn(Schedulers.single())
				.map(x -> {
					log.info("valor: " + x + " | thread: " + Thread.currentThread().getName());
					return x;
				})
				.subscribe();

		Thread.sleep(2000);
	}

	public void m16PublishSubscribeOn() throws InterruptedException{
		Flux.range(1, 2)
				.publishOn(Schedulers.parallel())
				.map(x -> {
					log.info("valor: " + x + " | thread: " + Thread.currentThread().getName());
					return x;
				})
				.subscribeOn(Schedulers.single())
				.map(x -> {
					log.info("valor: " + x + " | thread: " + Thread.currentThread().getName());
					return x;
				})
				.publishOn(Schedulers.boundedElastic()) //el publish es de mayor jerarquia que el subscribeon
				.map(x -> {
					log.info("valor: " + x + " | thread: " + Thread.currentThread().getName());
					return x;
				})
				.subscribe();

		Thread.sleep(2000);
	}

	public void m17runOn() throws InterruptedException {
		Flux.range(1, 16)
				.parallel(8) //con el numnero indico cuanto cors utilizo de mi pc
				.runOn(Schedulers.parallel())
				.map(x -> {
					log.info("valor: " + x + " | thread: " + Thread.currentThread().getName());
					return x;
				})
				//para regresar al flux despues del parallel flux
				//.sequential()
				.subscribe();
	}

	public static void main(String[] args) {
		dishes.add("Ceviche");
		dishes.add("Causa");
		dishes.add("Lomo Saltado");
		SpringApplication.run(SpringReactorDemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		m17runOn();
	}
}
