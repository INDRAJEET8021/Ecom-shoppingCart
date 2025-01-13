
    function toggleWishlist(event, icon) {
        event.preventDefault(); // Prevent the default behavior (clicking on a link)
        event.stopPropagation(); // Prevent the click event from propagating to the anchor tag
        icon.classList.toggle('active');
        // Add additional logic here to handle the wishlist action (e.g., sending a request to the server)
    }
