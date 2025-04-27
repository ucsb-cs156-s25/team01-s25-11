package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository; 

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;


@Tag(name = "MenuItemReviews")
@RequestMapping("/api/menuitemreviews")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController 
{
    @Autowired
    MenuItemReviewRepository menuItemReviewRepository;

    /**
     * List all menu item reviews 
     * 
     * @return an iterable of MenuItemReview
     */
    @Operation(summary= "List all menu item review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<MenuItemReview> allMenuItemReivew() 
    {
        Iterable<MenuItemReview> menuItemReview = menuItemReviewRepository.findAll();
        return menuItemReview;
    }

    /**
     * Create a new menu item review 
     * 
     * @param comments     
     * @param itemId 
     * @param reviewerEmail 
     * @param stars 
     * @return the saved menuItemReview 
     */
    @Operation(summary= "Create a MenuItemReview")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")

    public MenuItemReview postMenuItemReview 
    (
            @Parameter(name="comments") @RequestParam String comments,
            @Parameter(name="itemId") @RequestParam long itemId, 
            @Parameter(name="reviewerEmail") @RequestParam String reviewerEmail,
            @Parameter(name="stars") @RequestParam int stars 
    )
            throws JsonProcessingException 
            {

        MenuItemReview menuItemReview = new MenuItemReview();
        menuItemReview.setItemid(itemId); 
        menuItemReview.setComments(comments);
        menuItemReview.setRevieweremail(reviewerEmail); 
        menuItemReview.setStars(stars); 
        MenuItemReview savedMenuItemReview = menuItemReviewRepository.save(menuItemReview);

        return savedMenuItemReview;
    }

    /**
     * Get a single menu item review by id 
     * 
     * @param id the id of the menu item review 
     * @return a menu item review 
     */
    @Operation(summary= "Get a menu item review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public MenuItemReview getById(
            @Parameter(name="id") @RequestParam Long id) 
    {
        MenuItemReview menuItemReview = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        return menuItemReview;
    }

    /**
     * Update a single menu item review 
     * 
     * @param id       id of the review to update
     * @param incoming the new review
     * @return the updated review object
     */
    @Operation(summary= "Update a single menu item review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public MenuItemReview updateMenuItemReview(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid MenuItemReview incoming) 
    {

        MenuItemReview menuItemReview = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        menuItemReview.setItemid(incoming.getItemid()); 
        menuItemReview.setRevieweremail(incoming.getRevieweremail()); 
        menuItemReview.setComments(incoming.getComments());
        menuItemReview.setStars(incoming.getStars()); 

        menuItemReviewRepository.save(menuItemReview);

        return menuItemReview;
    }

}
