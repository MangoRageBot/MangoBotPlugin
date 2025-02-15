document.addEventListener("DOMContentLoaded", function () {
    const dropArea = document.getElementById("drop-area");
    const fileInput = document.getElementById("file-input");
    const upload = document.getElementById("upload");
    const form = document.querySelector("form"); // Get the form element

    function updateDropArea(files) {
        // Find the existing <p> inside drop-area (or create a new one if it doesn't exist)
        let paragraph = dropArea.querySelector("p");
        if (!paragraph) {
            paragraph = document.createElement("p");
            dropArea.appendChild(paragraph);
        }

        // Create a list of selected file names
        const fileNames = Array.from(files).map(file => file.name).join(", ");

        // Set the selected file names in the <p> element
        paragraph.textContent = `Selected files: ${fileNames}`;

        // Enable the upload button if there are files selected, otherwise disable it
        upload.disabled = files.length === 0;
        if (upload.disabled) {
            upload.style.backgroundColor = ""; // Reset color if button is disabled
        }
    }

    // Allow drag over
    dropArea.addEventListener("dragover", function (e) {
        e.preventDefault();
        dropArea.classList.add("highlight");
    });

    // Remove highlight when drag leaves
    dropArea.addEventListener("dragleave", function () {
        dropArea.classList.remove("highlight");
    });

    // Handle drop event
    dropArea.addEventListener("drop", function (e) {
        e.preventDefault();
        dropArea.classList.remove("highlight");

        const files = e.dataTransfer.files; // Get the dropped files
        fileInput.files = e.dataTransfer.files; // Assign the files to the file input
        updateDropArea(files);
    });

    // Allow user to click on the drop area to open the file dialog
    dropArea.addEventListener("click", function () {
        fileInput.click();
    });

    // When a file is selected via the file input
    fileInput.addEventListener("change", function () {
        if (fileInput.files.length > 0) {
            updateDropArea(fileInput.files);
        }
    });

    // Disable the upload button initially
    upload.disabled = true;

    // Handle upload button click
    upload.addEventListener("click", function () {
        if (upload.disabled) return; // Prevent click if button is disabled

        // Disable the button, make it red, and show the text "Please Wait, uploading..."
        upload.disabled = true;
        upload.style.backgroundColor = "red"; // Change color to red
        upload.value = "Please Wait, uploading..."; // Update text

        form.submit(); // Submit the form manually
    });
});
