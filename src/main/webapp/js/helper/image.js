function validateImageUpload(inputElement) {
    // Working with the passed `inputElement` directly
    const file = inputElement.files[0];
    if (file) {
        const fileName = file.name.toLowerCase();
        const allowedExtensions = /(\.jpg|\.jpeg|\.png)$/i;
        if (!allowedExtensions.test(fileName)) {
            inputElement.value = '';
        }
    }
}