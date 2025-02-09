document.addEventListener("DOMContentLoaded", function () {
    const dropArea = document.getElementById("drop-area");
    const fileInput = document.getElementById("file-input");

    function updateDropArea(file) {
        // Find the existing <p> inside drop-area (or create a new one if it doesn't exist)
        let paragraph = dropArea.querySelector("p");
        if (!paragraph) {
            paragraph = document.createElement("p");
            dropArea.appendChild(paragraph);
        }

        // Set the selected file name in the <p> element
        paragraph.textContent = `Selected file: ${file.name}`;
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

        const file = e.dataTransfer.files[0]; // Get the first dropped file
        fileInput.files = e.dataTransfer.files; // Assign the file to the file input
        updateDropArea(file)
    });

    // Allow user to click on the drop area to open file dialog
    dropArea.addEventListener("click", function () {
        fileInput.click();
    });

    // When a file is selected via the file input
    fileInput.addEventListener("change", function (e) {
        if (fileInput.files.length > 0) {
            updateDropArea(fileInput.files[0])
        }
    });
});

