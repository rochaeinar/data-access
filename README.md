# Data Access Layer Configuration

* Add the JitPack repository to your "build.gradle" (project file) at the end of repositories:

		allprojects {
			repositories {
			        //..
			        maven { url "https://jitpack.io" }
			}
		}

* Add the dependency in "build.gradle" (module file)

		dependencies {
		        compile 'com.github.rochaeinar:data-access:1.0.8'
		}
# Table Example
		@Table(name = "OptionalTableName")
		public class Marcador extends Entity{
		
		    @PrimaryKey
		    @Field(name = "OptionalDatabaseColumnName")
		    public long id;
	
		    @Field
		    public String description;
	
		    @Field
		    public char code;
	
		    @Field
		    public int language;
	
		    @Field
		    public boolean status;
	
		    @Field
		    public Date date;
	
		    @Field
		    public short myShort;
	
		    @Field
		    public double myDouble;
	
		    @Field
		    public float myFloat;
		}
#Initialize database
		DB db = new DB(getApplicationContext());
#Save
		Marcador t = new Marcador();
		db.save(t);
#Update
		t.description="changes";
		db.save(t);
#Get by id
		Marcador m2 = db.getById(Marcador.class, 1);
#Get All
		ArrayList<Marcador> entities = db.getAll(Marcador.class);
	        for (Marcador entity : entities) {
	            Log.w(entity.toString());
	        }
#Options
	        Options options = new Options();
	        options.and("description", "");
	        options.and("code", "");
	        options.and("status", false);
	        options.and("date", null);
	        options.orderBy("id", true);
	        options.distinct(true);
	        options.in("id", new ArrayList(Arrays.asList(5, 30)));
	        options.limit(5);
	        entities = db.getAll(Marcador.class, options);
	        for (Marcador entity : entities) {
	            Log.w(entity.toString());
	        }
	
	        Log.i("count: " + db.calculate(Marcador.class, Aggregation.count()) + "");
	
	        //Equivalent query
	        //
	        //		SELECT DISTINCT *
	        //		FROM Marcador
	        //		WHERE Marcador.status = 0
	        //			AND Marcador.id IN (5,30)
	        //			ORDER BY Marcador.id ASC
	        //			LIMIT 5;

