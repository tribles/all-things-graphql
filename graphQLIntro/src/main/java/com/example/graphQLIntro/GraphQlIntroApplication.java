package com.example.graphQLIntro;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.graphQLIntro.exception.GraphQLErrorAdapter;
import com.example.graphQLIntro.model.Author;
import com.example.graphQLIntro.model.Book;
import com.example.graphQLIntro.repository.AuthorRepository;
import com.example.graphQLIntro.repository.BookRepository;
import com.example.graphQLIntro.resolver.BookResolver;
import com.example.graphQLIntro.resolver.Mutation;
import com.example.graphQLIntro.resolver.Query;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.servlet.GraphQLErrorHandler;

@SpringBootApplication
public class GraphQlIntroApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphQlIntroApplication.class, args);
	}
	
	@Bean
	public GraphQLErrorHandler errorHandler() {
		return new GraphQLErrorHandler() {
			@Override
			public List<GraphQLError> processErrors(List<GraphQLError> errors) {
				List<GraphQLError> clientErrors = errors.stream()
						.filter(this::isClientError)
						.collect(Collectors.toList());

				List<GraphQLError> serverErrors = errors.stream()
						.filter(e -> !isClientError(e))
						.map(GraphQLErrorAdapter::new)
						.collect(Collectors.toList());

				List<GraphQLError> e = new ArrayList<>();
				e.addAll(clientErrors);
				e.addAll(serverErrors);
				return e;
			}

			protected boolean isClientError(GraphQLError error) {
				return !(error instanceof ExceptionWhileDataFetching || error instanceof Throwable);
			}
		};
	}
	
	@Bean
	public BookResolver authorResolver(AuthorRepository authorRepository) {
		return new BookResolver(authorRepository);
	}


	@Bean
	public Query query(AuthorRepository authorRepository, BookRepository bookRepositor) {
		return new Query(authorRepository,bookRepositor);
	}
	
	@Bean
	public Mutation mutation(AuthorRepository authorRepository, BookRepository bookRepositor) {
	   return new Mutation(authorRepository,bookRepositor);	
	}
	
	@Bean
	public CommandLineRunner demo(AuthorRepository authorRepository, BookRepository bookRepository) {
		return (args) -> {
			Author author = new Author("Herbert", "Schildt");
			authorRepository.save(author);
			bookRepository.save(new Book("Java: A Beginner's Guide, Sixth Edition", "0071809252", 728, author));
		};
	}
}
