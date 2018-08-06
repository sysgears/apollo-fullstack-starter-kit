import fs from 'fs';
import mkdirp from 'mkdirp';
import shortid from 'shortid';

const UPLOAD_DIR = 'public';

interface StoreFsProps {
  filename: string;
  stream: any;
}

const storeFS = ({ stream, filename }: StoreFsProps): Promise<{ path: string; size: number }> => {
  // Check if UPLOAD_DIR exists, create one if not
  if (!fs.existsSync(UPLOAD_DIR)) {
    mkdirp.sync(UPLOAD_DIR);
  }

  const id = shortid.generate();
  const path = `${UPLOAD_DIR}/${id}-${filename}`;

  return new Promise((resolve, reject) =>
    stream
      .on('error', (error: Error) => {
        if (stream.truncated) {
          // Delete the truncated file
          fs.unlinkSync(path);
        }
        reject(error);
      })
      .pipe(fs.createWriteStream(path))
      .on('error', (error: Error) => reject(error))
      .on('finish', () => resolve({ path, size: fs.statSync(path).size }))
  );
};

interface ProcessUploadProps {
  filename: string;
  mimetype: string;
  stream: any;
}

const processUpload = async (uploadPromise: Promise<ProcessUploadProps>) => {
  const { stream, filename, mimetype } = await uploadPromise;
  const { path, size } = await storeFS({ stream, filename });

  return { name: filename, type: mimetype, path, size };
};

export default () => ({
  Query: {
    files(obj: any, args: any, { Upload }: any) {
      return Upload.files();
    }
  },
  Mutation: {
    async uploadFiles(obj: any, { files }: { files: [Promise<ProcessUploadProps>] }, { Upload, req }: any) {
      const { t } = req;

      try {
        const results = await Promise.all(files.map(processUpload));

        return Upload.saveFiles(results);
      } catch (e) {
        throw new Error(t('upload:fileNotLoaded'));
      }
    },
    async removeFile(obj: {}, { id }: { id: number }, { Upload, req }: any) {
      const file = await Upload.file(id);
      const { t } = req;

      if (!file || !(await Upload.deleteFile(id))) {
        throw new Error(t('upload:fileNotFound'));
      }

      // remove file
      try {
        fs.unlinkSync(file.path);
      } catch (e) {
        throw new Error(t('upload:fileNotDeleted'));
      }

      return true;
    }
  },
  Subscription: {}
});