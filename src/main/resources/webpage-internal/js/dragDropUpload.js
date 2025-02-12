document.addEventListener("DOMContentLoaded", function () {
    const dropArea = document.getElementById("drop-area");
    const fileInput = document.getElementById("file-input");

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

        const files = e.dataTransfer.files; // Get the first dropped file
        fileInput.files = e.dataTransfer.files; // Assign the file to the file input
        updateDropArea(files)
    });

    // Allow user to click on the drop area to open file dialog
    dropArea.addEventListener("click", function () {
        fileInput.click();
    });

    // When a file is selected via the file input
    fileInput.addEventListener("change", function (e) {
        if (fileInput.files.length > 0) {
            updateDropArea(fileInput.files)
        }
    });
});

