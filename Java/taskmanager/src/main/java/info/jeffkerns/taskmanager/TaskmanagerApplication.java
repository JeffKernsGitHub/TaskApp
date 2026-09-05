/**
 * ==============================================================================
 * TaskManager Application - Main Entry Point
 * ==============================================================================
 * This class serves as the starting point (bootstrap class) for the entire
 * Spring Boot application.
 *
 * Spring Boot simplifies Java development by:
 * 1. Auto-configuration: Automatically configuring libraries based on classpath dependencies.
 * 2. Standalone execution: Embedding an HTTP web server (Apache Tomcat by default),
 *    so you can run the app as a standard Java program without deploying a WAR file.
 * 3. Opinionated defaults: Providing pre-configured best practices out of the box.
 * ==============================================================================
 */

package info.jeffkerns.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * {@code @EnableSpringDataWebSupport} configures how Spring Data constructs and serializes
 * pagination information (e.g., {@code Page<T>}) across HTTP REST responses.
 *
 * <p>Why {@code VIA_DTO}?
 * In Spring Boot 3.3+, standardizing on {@code VIA_DTO} serializes pagination metadata
 * using a stable, dedicated Data Transfer Object (DTO) structure rather than directly
 * serializing Spring internal classes. This ensures client-friendly, consistent JSON output
 * (e.g., page number, page size, total elements, total pages).</p>
 */
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)

/**
 * {@code @SpringBootApplication} is a powerful convenience annotation that bundles three essential annotations:
 * <ul>
 *   <li>{@code @Configuration}: Marks this class as a source of bean definitions for the application context.</li>
 *   <li>{@code @EnableAutoConfiguration}: Tells Spring Boot to guess and configure beans that you are likely
 *       to need based on the jar dependencies added to your pom.xml (e.g., automatically configuring
 *       a database DataSource if H2/PostgreSQL is on the classpath).</li>
 *   <li>{@code @ComponentScan}: Tells Spring to recursively scan this package ({@code info.jeffkerns.taskmanager})
 *       and all of its subpackages for Spring-managed components (like {@code @Component}, {@code @Service},
 *       {@code @Repository}, and {@code @RestController}).</li>
 * </ul>
 */
@SpringBootApplication
public class TaskmanagerApplication {

	/**
	 * The standard Java application entry point.
	 *
	 * When you launch the program (e.g., running `java -jar taskmanager.jar` or running this in your IDE),
	 * the Java Virtual Machine (JVM) looks for this exact signature: {@code public static void main(String[] args)}.
	 *
	 * {@link SpringApplication#run(Class, String...)} does the heavy lifting:
	 * 1. Creates an appropriate ApplicationContext instance (web application context).
	 * 2. Starts the embedded web server (Tomcat).
	 * 3. Scans for Spring beans and injects dependencies.
	 * 4. Executes any startup runners (CommandLineRunner/ApplicationRunner).
	 *
	 * @param args command-line arguments passed to the application at launch
	 */
	public static void main(String[] args) {
		SpringApplication.run(TaskmanagerApplication.class, args);
	}

}

