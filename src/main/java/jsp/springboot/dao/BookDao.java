package jsp.springboot.dao;


import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import jsp.springboot.entity.Book;
import jsp.springboot.repository.BookRepository;

@Repository
@Slf4j
public class BookDao {

	@Autowired
	private BookRepository bookRepository;
	
	//to save a single book record
	public Book saveBook(Book book) {
		log.info("Book is being saved : {}", book);
		return bookRepository.save(book);
	}
	
	//fetch all books
	public List<Book> getAllBooks() {
		return bookRepository.findAll();
	}
	
	//fetch a book by id
	public Optional<Book> getBookById(int id) {
		log.info("Fetching book by id in repo {}", id);
		return bookRepository.findById(id);
	}
	
	//Update a Book record
	public Book updateBook(Book book) {
		return bookRepository.save(book);
	}
	
	//Delete a book by id
	public void deleteBookById(int id) {
		 bookRepository.deleteById(id);
	}
	
	//fetch or get a book by author name
	public List<Book> fetchBookByAuthor(String author) {
		return bookRepository.findByAuthor(author);
	}
	
	//fetch Book which has price greater than passed price
	public List<Book> fetchBookByGreaterThan(double price) {
		return bookRepository.findByPriceGreaterThan(price);
	}
	
	//fetch Book between price
	public List<Book> fetchBookByPriceBetween(double start,double end) {
		return bookRepository.findByPriceBetween(start,end);
	}
	
	//fetch Book by published year
	public List<Book> fetchBookByPublishedYear(int publishedYear) {
		return bookRepository.getBookByPublishedYear(publishedYear);
	}
	
	//fetch Book by availability
	public List<Book> fetchBookByAvailability(boolean availability) {
		return bookRepository.getBookByAvailability(availability);
	}
	
	//fetch Book by genre
	public List<Book> fetchBookGenre(String genre) {
		return bookRepository.getBookByGenre(genre);
	}
	
	//Pagination
	public Page<Book> fetchBookByPagination(int pageNumber, int pageSize) {
		return bookRepository.findAll(PageRequest.of(pageNumber, pageSize));
	}
	
	//Sorting 
	public List<Book> fetchBookBySorting(String field) {
		return bookRepository.findAll(Sort.by(field).ascending());
	}
	
	//Both Pagination and Sorting
	public Page<Book> fetchBookByPaginationAndSorting(int pageNumber, int pageSize, String field) {
		return bookRepository.findAll(PageRequest.of(pageNumber, pageSize,Sort.by(field).descending()));
	}
}
