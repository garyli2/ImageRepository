# ImageRepository Backend
#### An image repository backend featuring bulk image upload with authenticated user access and encryption at rest.

## How to use
#### For images of the frontend test pages, go to https://github.com/garyli2/ImageRepositoryFrontend
1. In a deployed application, make an account by clicking on the sign up button on the index page.
2. Fill in account details and register. You will then be taken to the login page to sign in.
3. After you login, click on the "My gallery" button at the top right.
4. Here, you have options to upload images with customizable permission, deleting images via single, bulk or all and a table to view all uploads.
5. The "public gallery" page is available at the top navagation bar. This is where all the public images are displayed.


## Features
- Secure upload, with transmissions protected by TLS 1.2 (if you choose to enable so)
- Authenticated user access, accounts are used to keep track of your uploads and prevent others from deleting your own images.
- Public/private access, set whichever permission is most appropriate for you!
- Public gallery feature, where public images are listed.
- Bulk uploads, with a max total submission size of 100MB! You can uploads thousands of files at once.
- Bulk delete, clear your entire repository or delete select amounts of images.
