package boa.datascience.internalDataStorage.hadoopSequenceFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ReflectionUtils;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

import boa.datascience.externalDataSources.DatagenProperties;
import boa.datascience.internalDataStorage.AbstractDataStorage;

public class SequenceFileStorage extends AbstractDataStorage {
	private Configuration conf;
	SequenceFile.Reader seqFileReader;
	SequenceFile.Writer seqFileWriter;

	public SequenceFileStorage(String location, String parser) {
		super(location, parser);
		conf = new Configuration();
	}

	@Override
	public boolean isAvailable(String source) {
		org.apache.hadoop.io.Text key = (org.apache.hadoop.io.Text) ReflectionUtils
				.newInstance(seqFileReader.getKeyClass(), conf);
		org.apache.hadoop.io.BytesWritable keyValue = (org.apache.hadoop.io.BytesWritable) ReflectionUtils
				.newInstance(seqFileReader.getValueClass(), conf);
		try {
			this.seqFileReader.next(key, keyValue);
			AbstractDataStorage.LOG.info(key.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void store(List<GeneratedMessage> dataInstance) {
		this.openWriter(DatagenProperties.HADOOP_SEQ_FILE_LOCATION + "/" + DatagenProperties.HADOOP_SEQ_FILE_NAME);
		dataInstance.stream().forEach(data -> {
			try {
				this.seqFileWriter.append(new Text("data1"), new BytesWritable(data.toByteArray()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		this.closeWrite();
	}

	@Override
	public void storeAt(String location, GeneratedMessage dataInstance) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<GeneratedMessage> getData() {
		return this.getData(this.parser);
	}

	@Override
	public boolean getDataInQueue(Queue<GeneratedMessage> q) {
		// TODO Auto-generated method stub
		return false;
	}

	protected List<GeneratedMessage> getData(Method parser) {
		this.openReader(DatagenProperties.HADOOP_SEQ_FILE_LOCATION + "/" + DatagenProperties.HADOOP_SEQ_FILE_NAME);
		List<GeneratedMessage> data = new ArrayList<>();

		org.apache.hadoop.io.Text key = (org.apache.hadoop.io.Text) ReflectionUtils
				.newInstance(this.seqFileReader.getKeyClass(), conf);
		org.apache.hadoop.io.BytesWritable keyValue = (org.apache.hadoop.io.BytesWritable) ReflectionUtils
				.newInstance(this.seqFileReader.getValueClass(), conf);

		try {
			while (this.seqFileReader.next(key, keyValue)) {
				data.add((GeneratedMessage) this.parser.invoke(null, com.google.protobuf.CodedInputStream
						.newInstance(keyValue.getBytes(), 0, keyValue.getLength())));
			}
		} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}

	private boolean openReader(String seqPath) {
		Path path = new Path(seqPath);
		FileSystem fs;
		try {
			fs = FileSystem.get(conf);
			this.seqFileReader = new SequenceFile.Reader(fs, path, conf);
			return true;
		} catch (IOException e) {
			System.out.println("Exception occured in Program node while creating FileSystem or Reader");
			e.printStackTrace();
			return false;
		}
	}

	private boolean openWriter(String seqPath) {
		FileSystem fileSystem;
		try {
			fileSystem = FileSystem.get(conf);
			this.seqFileWriter = SequenceFile.createWriter(fileSystem, conf, new Path(seqPath), Text.class,
					BytesWritable.class);
			this.seqFileWriter = SequenceFile.createWriter(fileSystem, conf, new Path(seqPath), Text.class,
					BytesWritable.class);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean closeReader() {
		try {
			seqFileReader.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private boolean closeWrite() {
		try {
			seqFileWriter.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	@Override
	public String getDataLocation() {
		return DatagenProperties.HADOOP_SEQ_FILE_LOCATION;
	}

}
