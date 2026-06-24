function showToast(message, type) {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);

    gsap.to(toast, {
        x: 0,
        duration: 0.3,
        ease: 'power2.out'
    });

    setTimeout(function() {
        gsap.to(toast, {
            x: '100%',
            duration: 0.3,
            ease: 'power2.in',
            onComplete: function() {
                toast.remove();
            }
        });
    }, 3000);
}

function animateButton(btn) {
    gsap.to(btn, {
        scale: 0.95,
        duration: 0.1,
        yoyo: true,
        repeat: 1
    });
}

function fadeIn(element, duration = 0.5, delay = 0) {
    gsap.from(element, {
        opacity: 0,
        duration: duration,
        delay: delay,
        ease: 'power2.out'
    });
}

function slideUp(element, duration = 0.5, delay = 0) {
    gsap.from(element, {
        opacity: 0,
        y: 30,
        duration: duration,
        delay: delay,
        ease: 'power2.out'
    });
}

function scaleIn(element, duration = 0.5, delay = 0) {
    gsap.from(element, {
        opacity: 0,
        scale: 0.9,
        duration: duration,
        delay: delay,
        ease: 'back.out(1.7)'
    });
}