package hdp2wordcount;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class WordCount {

	public static class IntSumReducer extends
			Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}

	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
		private boolean usingKeywordFile = true;
		private List<String> keywords;
		private List<String> matchingWords;

		TokenizerMapper(){
			//Read from the YARN Local Cache.  
			//This is also the same way to read from the cache if you add via the job launcher.
			try (BufferedReader br = new BufferedReader(new FileReader("./keywords"))) { //read symlinked file
			    keywords = new ArrayList<String>(Arrays.asList(br.readLine().split(",")));
			} catch (FileNotFoundException e) {
				usingKeywordFile=false;
			} catch (IOException e) {
				usingKeywordFile=false;
			}
		}
		
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {			
			//Check if using KeyWord File or not.
			//If using it, then filter only on keywords
			if(usingKeywordFile){
				matchingWords = new ArrayList<String>(Arrays.asList(value.toString().split(",")));
				matchingWords.retainAll(keywords);
				Iterator<String> itr = matchingWords.iterator();
				
				while (itr.hasNext()) {
					word.set(itr.next());
					context.write(word, one);
				}
			}
			else{ //If not using keyword file just do a basic wordcount
				StringTokenizer itr = new StringTokenizer(value.toString(), ",");
				while (itr.hasMoreTokens()) {
					word.set(itr.nextToken());
					context.write(word, one);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "word count");
		job.setJarByClass(WordCount.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
	
		TextInputFormat.addInputPath(job, new Path(args[0]));
		TextOutputFormat.setOutputPath(job, new Path(args[1]));
		
		//If being ran as a normal MR job see if someone is passing a keywords list
		if(args.length==3)
			job.addCacheFile(new URI(args[2]+"#keywords")); //symlink the file so we don't care what its named on HDFS
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}