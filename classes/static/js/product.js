function toggleWishlist(event, productId, icon) {
    event.preventDefault(); // Prevent default link behavior

    fetch(`/user/watchlist/toggle?productId=${productId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok.');
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            if (data.added) {
                icon.classList.add('active'); // Add 'active' class if added
                alert('Product successfully added to wishlist.');
            } else {
                icon.classList.remove('active'); // Remove 'active' class if removed
                alert('Product successfully removed from wishlist.');
            }
        } else {
            alert('Error toggling wishlist item.');
        }
                    updateNavbarWishlistCount();

    })
    .catch(error => {
        console.error('Error:', error);
        alert('An error occurred while toggling the wishlist item.');
    });
}


document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.wishlist-icon').forEach(icon => {
        const productId = icon.getAttribute('data-product-id');
        fetch(`/user/check-watchlist-status?productId=${productId}`)
            .then(response => response.json())
            .then(isInWatchlist => {
                if (isInWatchlist) {
                    icon.classList.add('active');
                }
            })
            .catch(error => {
                console.error('Error fetching watchlist status:', error);
            });
    });
});
