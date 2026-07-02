import os
import zipfile

def zipdir(path, ziph):
    for root, dirs, files in os.walk(path):
        # Exclude massive or unnecessary directories
        dirs[:] = [d for d in dirs if d not in ['.git', 'node_modules', 'target', '.metadata', '.settings', '.vscode']]
        
        for file in files:
            # Prevent zipping the python script or the zip file itself
            if file in ['zip_project.py', 'resume_builder_project_complete.zip']:
                continue
            
            file_path = os.path.join(root, file)
            arcname = os.path.relpath(file_path, os.path.dirname(path))
            ziph.write(file_path, arcname)

if __name__ == '__main__':
    project_dir = r"c:\Users\Admin\OneDrive\Desktop\Core java\resume_builder_project"
    zip_path = r"c:\Users\Admin\OneDrive\Desktop\Core java\resume_builder_project_complete.zip"
    
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        zipdir(project_dir, zipf)
        
    print(f"Successfully created zip file at: {zip_path}")
