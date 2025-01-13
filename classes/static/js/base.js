// base.js or similar file loaded across your app

// Function to update wishlist count in the navigation bar
function updateWishlistCount() {
    fetch('/user/get-wishlist-count')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Update the wishlist count badge
                const badge = document.querySelector('.wishlist-badge');
                if (badge) {
                    badge.textContent = data.wishlistCount;
                }
            } else {
                console.error('Error fetching wishlist count:', data.error);
            }
        })
        .catch(error => console.error('Error:', error));
}

// Call the function when the DOM is loaded
document.addEventListener('DOMContentLoaded', updateWishlistCount);

// Update wishlist count when a product is added or removed
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
            // Update the icon color based on the action
            if (data.added) {
                icon.classList.add('active'); // Add 'active' class if added
                alert('Product successfully added to wishlist.');
            } else {
                icon.classList.remove('active'); // Remove 'active' class if removed
                alert('Product successfully removed from wishlist.');
            }
            // Update the wishlist count in the navigation bar
            updateWishlistCount();
        } else {
            alert('Error toggling wishlist item.');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('An error occurred while toggling the wishlist item.');
    });
}
