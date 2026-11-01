package jsp.springboot.controller;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jsp.springboot.dto.ServerResponse;
import jsp.springboot.entity.Book;
import jsp.springboot.service.BookService;

@RestController
@RequestMapping("/api/books")
@Slf4j
public class BookController {
	
	
	@Autowired
	private BookService bookService;
	
	//insert operation
	@PostMapping
	public ResponseEntity<ServerResponse<Book>> saveBook(@RequestBody Book book) {
		log.info("Book saving api us called with payload: {}", book);
		return bookService.saveBook(book);
	}
	
	//fetching or retrieving operation
	@GetMapping
	public ResponseEntity<ServerResponse<List<Book>>> getAllBooks() {
		return bookService.getAllBooks();
	}
	
	//fetching a record by Id
	@GetMapping("/{id}")
	public ResponseEntity<ServerResponse<Map<String,Object>>> fetchBookById(@PathVariable int id , @RequestParam(value = "tutorial", defaultValue = "1") int tutorialId , @RequestParam(value = "product", defaultValue = "1") int productId) {
		log.info("Fetching book by id {}", id);
		log.info("Fetching Tutorial by id {}", tutorialId);
		log.info("Fetching Product by id {}", productId);
		return bookService.getBookById(id , tutorialId, productId);
	}
	
	//update a book record
	@PutMapping
	public ResponseEntity<ServerResponse<Book>> updateBook(@RequestBody Book book) {
		return bookService.updateBook(book);
	}
	
	//delete a book by id
	@DeleteMapping("/{id}")
	public ResponseEntity<ServerResponse<Book>> deleteBookById(@PathVariable int id) {
	    return bookService.deleteBookById(id);
	}

	//fetch a book record by author name
	@GetMapping("/author/{author}")
	public ResponseEntity<ServerResponse<List<Book>>> getBookByAuthor(@PathVariable String author) {
		return bookService.fetchBookByAuthor(author);
	}
	
	//get a book record by price
	@GetMapping("/price/{price}")
	public ResponseEntity<ServerResponse<List<Book>>> getBookGreaterThan(@PathVariable double price) {
		return bookService.getBookByPriceGreaterThan(price);
	}
	
	//get a book record between price range
	@GetMapping("/price/{start}/{end}")
	public ResponseEntity<ServerResponse<List<Book>>> getBooksByPriceRange(@PathVariable double start, @PathVariable double end) {
		return bookService.getBookByPriceBetween(start, end);
	}
	
	//get a book record by published year
	@GetMapping("/publishedYear/{publishedYear}")
	public ResponseEntity<ServerResponse<List<Book>>> fetchBookByPublishedYear(@PathVariable int publishedYear) {
		return bookService.getBookByPublishedYear(publishedYear);
	}
	
	//get a book record by its availability
	@GetMapping("/availability/{availability}")
	public ResponseEntity<ServerResponse<List<Book>>> fetchBookByAvailabilty(@PathVariable boolean availability) {
		return bookService.getBookByAvailability(availability);
	}
	
	//get a book by genre
	@GetMapping("/genre/{genre}")
	public ResponseEntity<ServerResponse<List<Book>>> fetchBookByGenre(@PathVariable String genre) {
		return bookService.getBookByGenre(genre);
	}
	
	//get a book with pagination
	@GetMapping("/page/{pageNumber}/{pageSize}")
	public ResponseEntity<ServerResponse<Page<Book>>> fetchBookByPagination(@PathVariable int pageNumber, @PathVariable int pageSize) {
		return bookService.getBookByPagination(pageNumber, pageSize);
	}
	
	//get a book by sorting
	@GetMapping("/sort/{field}")
	public ResponseEntity<ServerResponse<List<Book>>> fetchBookBySorting(@PathVariable String field) {
		return bookService.getBookBySorting(field);
	}
	
	//combination of pagination and sorting
	@GetMapping("/page/{pageNumber}/{pageSize}/sort/{field}")
	public ResponseEntity<ServerResponse<Page<Book>>> fetchBookByPaginationAndSorting(@PathVariable int pageNumber, @PathVariable int pageSize, @PathVariable String field) {
		return bookService.getBookByPaginationAndSorting(pageNumber, pageSize, field);
	}
}

