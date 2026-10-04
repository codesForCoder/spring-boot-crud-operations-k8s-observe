package jsp.springboot.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import jsp.springboot.dto.Product;
import jsp.springboot.dto.Tutorial;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jsp.springboot.dao.BookDao;
import jsp.springboot.dto.ServerResponse;
import jsp.springboot.entity.Book;
import jsp.springboot.exception.NoRecordFoundException;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookService {


	private final  BookDao bookDao;
	private final RestClient restClient;
	@Value("${go.product-service.url}")
	private String productServiceUrl;
	@Value("${node.tutorial-service.url}")
	private String nodeTutorialServiceUrl;
	private final Tracer tracer =
			GlobalOpenTelemetry.getTracer("jsp.springboot.service.BookService");
	//Save book
	public ResponseEntity<ServerResponse<Book>> saveBook(Book book) {
		log.info("Book saving api us called with payload: {}", book);
		ServerResponse<Book> response = new ServerResponse<Book>();
		response.setStatusCode(HttpStatus.CREATED.value());
		response.setMessage("Book record saved");
		var item = bookDao.saveBook(book);
		response.setData(item);
		log.info("Book is saved successfully : {}", item);
		return new ResponseEntity<ServerResponse<Book>>(response,HttpStatus.CREATED);
	}
	
	//find all books
	public ResponseEntity<ServerResponse<List<Book>>> getAllBooks() {
		ServerResponse<List<Book>> response = new ServerResponse<List<Book>>();
		List<Book> books = bookDao.getAllBooks();
		List<Product> products = getProducts();
		if(books.size() > 0) {
			response.setStatusCode(HttpStatus.OK.value());
			response.setMessage("Fetched all the books");
			response.setData(books); 
			
			return new ResponseEntity<ServerResponse<List<Book>>> (response, HttpStatus.OK);
		}
		else 
			throw new NoRecordFoundException("No records found");
	}

	public  List<Product> getProducts() {

		return restClient
				.get()
				.uri(productServiceUrl + "/products")
				.retrieve()
				.body(new ParameterizedTypeReference<List<Product>>() {});
	}
	
	
	//fetch Book by Id
	public ResponseEntity<ServerResponse<Map<String,Object>>> getBookById(int id) {
		log.info("Fetching book by id from service {}", id);
		Span span = Span.current();
		ServerResponse<Map<String,Object>> response = new ServerResponse<>();
		Optional<Book> optionalBook = bookDao.getBookById(id);
		log.info("Fetched book by id from service {}", optionalBook);
		Optional<Product> optionalProduct = getProductById(id);
		log.info("Fetched product by id from service {}", optionalProduct);
		Optional<Tutorial> optionalTutorial = getTutorialById(id);
		log.info("Fetched tutorial by id from service {}", optionalTutorial);
		Map<String , Object> results = new HashMap<>();
		if(optionalBook.isPresent()) {
			log.info("Book found with id {}", optionalBook.get());
			results.put("book", optionalBook.get());
		}
		if(optionalProduct.isPresent()) {
			log.info("Product found with id {}", optionalProduct.get());
			results.put("product", optionalProduct.get());
		}
		if(optionalTutorial.isPresent()) {
			log.info("Tutorial found with id {}", optionalTutorial.get());
			results.put("tutorial", optionalTutorial.get());
		}
		if(results.containsKey("book")&& results.containsKey("product") && results.containsKey("tutorial")) {
			response.setStatusCode(HttpStatus.OK.value());
			response.setMessage("Found a Book by id : "+id);
			response.setData(results);
			span.setStatus(StatusCode.OK);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		else {
			log.info("Book not found with id {}", id);
				RuntimeException e =  new NoRecordFoundException("There is no record with the id " + id);
				span.recordException(e);
				span.setStatus(StatusCode.ERROR, "There is no record with the id " + id);
			throw e;
		}
	}

	public Optional<Product> getProductById(int id) {
		Span span = tracer.spanBuilder("product-service.get-product")
				.startSpan();

		try (var scope = span.makeCurrent()) {
		log.info("Fetching product by id from GOLANG CRUD service {}", id);
		try {
			Product product = restClient
					.get()
					.uri(productServiceUrl + "/products/{id}", id)
					.retrieve()
					.body(Product.class);
			log.info("Product found with id {} - {}", id ,product);
			span.setStatus(StatusCode.OK);
			return Optional.ofNullable(product);

		} catch (HttpClientErrorException.NotFound e) {
			log.info("Product not found with id {}", id);
			span.recordException(e);
			span.setStatus(StatusCode.ERROR, "There is no record with the id for product " + id);
			return Optional.empty();
		}
		} catch (Exception e) {

			span.recordException(e);
			span.setStatus(StatusCode.ERROR, "Failed to get product");

			throw e;

		} finally {
			span.end();
		}
	}

	public Optional<Tutorial> getTutorialById(int id) {
		Span span = tracer.spanBuilder("tutorial-service.get-tutorial")
				.startSpan();

		try (var scope = span.makeCurrent()) {
			log.info("Fetching tutorial by id from Nodejs CRUD service {}", id);
			try {
				Tutorial tutorial = restClient
						.get()
						.uri(nodeTutorialServiceUrl + "/api/tutorials/{id}", id)
						.retrieve()
						.body(Tutorial.class);
				log.info("Tutorial found with id {} - {}", id ,tutorial);
				span.setStatus(StatusCode.OK);
				return Optional.ofNullable(tutorial);

			} catch (HttpClientErrorException.NotFound e) {
				log.info("Tutorial not found with id {}", id);
				span.recordException(e);
				span.setStatus(StatusCode.ERROR, "There is no record with the id for tutorial " + id);
				return Optional.empty();
			}
		} catch (Exception e) {

			span.recordException(e);
			span.setStatus(StatusCode.ERROR, "Failed to get tutorial");

			throw e;

		} finally {
			span.end();
		}
	}
	
	//Update a book
	public ResponseEntity<ServerResponse<Book>> updateBook(Book book) {
		ServerResponse<Book> response = new ServerResponse<Book>();
		Optional<Book> optional = bookDao.getBookById(book.getId());
		
		if(optional.isPresent()) {
			response.setStatusCode(HttpStatus.OK.value());
			response.setMessage("Updated a Record Successfully!");
			response.setData(bookDao.saveBook(book));
			
			return new ResponseEntity<ServerResponse<Book>>(response,HttpStatus.OK);
		}
		else {
			response.setStatusCode(HttpStatus.NOT_FOUND.value());
			response.setMessage("Record updation failure!");
			
			return new ResponseEntity<ServerResponse<Book>>(response,HttpStatus.NOT_FOUND);
		}
	}
	
	//delete book by id
	public ResponseEntity<ServerResponse<Book>> deleteBookById(int id) {
	    ServerResponse<Book> response = new ServerResponse<>();
	    Optional<Book> optional = bookDao.getBookById(id);

	    if (optional.isPresent()) {
	        bookDao.deleteBookById(id);
	        response.setStatusCode(HttpStatus.OK.value());
	        response.setMessage("Deleted record with id " + id + " successfully");
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    } else {
	        throw new NoRecordFoundException("There is no record with id " + id);
	    }
	}

	//fetch a book by author
	public ResponseEntity<ServerResponse<List<Book>>> fetchBookByAuthor(String author) {
		List<Book> books = bookDao.fetchBookByAuthor(author);
		ServerResponse<List<Book>> response = new ServerResponse<List<Book>>();
		
		if(books.size() > 0) {
			response.setStatusCode(HttpStatus.OK.value());
			response.setMessage("Record found with the author name "+author);
			response.setData(books);
			
			return new ResponseEntity<ServerResponse<List<Book>>>(response,HttpStatus.OK);
		}
		else 
			throw new NoRecordFoundException("There are no books of "+author);
	}
	
	//fetch a book price greater than 
	public ResponseEntity<ServerResponse<List<Book>>> getBookByPriceGreaterThan(double price) {
		List<Book> books = bookDao.fetchBookByGreaterThan(price);
		ServerResponse<List<Book>> response = new ServerResponse<List<Book>>();
		
		if(books.size() > 0) {
			response.setStatusCode(HttpStatus.OK.value());
			response.setMessage("Books found");
			response.setData(books);
			
			return new  ResponseEntity<ServerResponse<List<Book>>>(response,HttpStatus.OK);
		}
		else 
			throw new NoRecordFoundException("Sorry No Books Found");
	}
	
	//fetch a Book by price between
	public ResponseEntity<ServerResponse<List<Book>>> getBookByPriceBetween(double start, double end) {
		ServerResponse<List<Book>> response = new ServerResponse<List<Book>>();
		
		List<Book> books = bookDao.fetchBookByPriceBetween(start, end);
		
		if(books.size() > 0) {
			response.setStatusCode(HttpStatus.OK.value());
			response.setMessage("Books found in between the specified range");
			response.setData(books);
			
			return new ResponseEntity<ServerResponse<List<Book>>>(response,HttpStatus.OK);
		}
		else 
			throw new NoRecordFoundException("No Books between "+start+ " and "+end+" 😐");
	}
	
	//fetch a book by published year
	public ResponseEntity<ServerResponse<List<Book>>> getBookByPublishedYear(int publishedYear) {
		ServerResponse<List<Book>> response  = new ServerResponse<List<Book>>();
		List<Book> books = bookDao.fetchBookByPublishedYear(publishedYear);
		
		if(books.size() > 0) {
			response.setStatusCode(HttpStatus.OK.value());
			response.setMessage("Found a Records with the published year "+publishedYear);
			response.setData(books);
			
			return new ResponseEntity<ServerResponse<List<Book>>>(response,HttpStatus.OK);
		}
		else 
			throw new NoRecordFoundException("No Records found to fetch");
	}
	
	//fetch a book by availability
	public ResponseEntity<ServerResponse<List<Book>>> getBookByAvailability(boolean availability) {
		ServerResponse<List<Book>> response = new ServerResponse<List<Book>>();
		List<Book> books = bookDao.fetchBookByAvailability(availability);
		
		if(books.size() > 0) {
			response.setStatusCode(HttpStatus.OK.value());
			response.setMessage("Found a Records");
			response.setData(books);
			
			return new ResponseEntity<ServerResponse<List<Book>>>(response,HttpStatus.OK);
		}
		else 
			throw new NoRecordFoundException("Sorry No Records found");
	}
	
	//fetch a book by genre
	public ResponseEntity<ServerResponse<List<Book>>> getBookByGenre(String genre) {
		ServerResponse<List<Book>> response = new ServerResponse<List<Book>>();
		List<Book> books = bookDao.fetchBookGenre(genre);
		
		if(books.size() > 0) {
			response.setStatusCode(HttpStatus.OK.value());
			response.setMessage("Found a Records");
			response.setData(books);
			
			return new ResponseEntity<ServerResponse<List<Book>>>(response,HttpStatus.OK);
		}
		else 
			throw new NoRecordFoundException("No records found");
	}
	
	//fetch a book by pagination
	public ResponseEntity<ServerResponse<Page<Book>>> getBookByPagination(int pageNumber, int pageSize) {
		ServerResponse<Page<Book>> response = new ServerResponse<Page<Book>>();
		Page<Book> pages  =bookDao.fetchBookByPagination(pageNumber, pageSize);
		
		if(!pages.isEmpty()) {
			response.setStatusCode(HttpStatus.OK.value());
			response.setMessage("Book records retrieved");
			response.setData(pages);
			
			return new ResponseEntity<ServerResponse<Page<Book>>>(response,HttpStatus.OK);
		} else 
			throw new NoRecordFoundException("No records found");
	}
	
	//fetch a book by sorting
	public ResponseEntity<ServerResponse<List<Book>>> getBookBySorting(String field) {
		ServerResponse<List<Book>> response = new ServerResponse<List<Book>>();
		List<Book> books = bookDao.fetchBookBySorting(field);
		
		if(books.size() > 0) {
			response.setStatusCode(HttpStatus.OK.value());
			response.setMessage("Sorted Book Records retrieved");
			response.setData(books);
			
			return new ResponseEntity<ServerResponse<List<Book>>>(response,HttpStatus.OK);
		} else 
			throw new NoRecordFoundException("No records found");		
	}
	
	//fetch a book by both pagination and sorting
	public ResponseEntity<ServerResponse<Page<Book>>> getBookByPaginationAndSorting(int pageNumber, int pageSize, String field) {
		ServerResponse<Page<Book>> response = new ServerResponse<Page<Book>>();
		
		Page<Book> pages = bookDao.fetchBookByPaginationAndSorting(pageNumber, pageSize, field);
		
		if(!pages.isEmpty()) {
			response.setStatusCode(HttpStatus.OK.value());
			response.setMessage("Paginated and Sorted the Book records");
			response.setData(pages);
			
			return new ResponseEntity<ServerResponse<Page<Book>>>(response,HttpStatus.OK);
		}
		else 
			throw new NoRecordFoundException("No records found");
	}
}
