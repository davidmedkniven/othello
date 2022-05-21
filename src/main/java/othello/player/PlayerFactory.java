package othello.player;


import othello.game.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;
import java.util.jar.*;


public class PlayerFactory {

    public String[] availablePlayers;
    private Map<String, Class> playerMap;
    private final String AGENT_PACKAGE = "othello.player.agents";

    public PlayerFactory() {
        getAgentClassesFromPackage();
    }

    public Agent newPlayer(String name, Color color) {
        return mapClassToPlayerObject(playerMap.get(name), color);
    }

    private void getAgentClassesFromPackage() {
        this.playerMap = new HashMap<String, Class>();

        Class[] classes = null;
        try {
            classes = getClasses(AGENT_PACKAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final List<String> playerList = new LinkedList<String>();

        for(Class c : classes) {
            if(Agent.class.isAssignableFrom(c)) {
                this.playerMap.put(c.getSimpleName(), c);
                playerList.add(c.getSimpleName());
            }
        }
        this.availablePlayers = new String[playerList.size()];
        for(int i = 0; i < playerList.size(); i++) {
            availablePlayers[i] = playerList.get(i);
        }
    }

    private Agent mapClassToPlayerObject(Class<Agent> clazz, Color color) {

        Constructor<Agent> constructor = null;
        Agent agent = null;
        try {
            constructor = clazz.getConstructor(Color.class);
            return (Agent) constructor.newInstance(color);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return agent;
    }

    private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        final String path = packageName.replace('.', '/');
        final URL resource = classLoader.getResource(path);
        
        final List<Class> classes = new ArrayList<Class>();
        // in case we run with: java -jar othello-xyz.jar
        if (resource.getProtocol().equals("jar")) {
            final String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!")); //strip out only the JAR file
            final JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries
            while(entries.hasMoreElements()) {
                final String name = entries.nextElement().getName();
                if (name.endsWith("/")) { continue; }
                if (name.startsWith(path)) { //filter according to the path
                    String entryPkg = name.substring(path.length()+1).replace('/', '.');
                    // Remove ".class"
                    entryPkg=entryPkg.substring(0, entryPkg.length() - 6);
                    classes.add(Class.forName(packageName + "." + entryPkg));
                }
            }
        }
        else {
            classes.addAll(findClasses(new File(resource.getFile()), packageName));
        }
            
        return classes.toArray(new Class[classes.size()]);
    }

    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        final List<Class> classes = new ArrayList<Class>();
        if ( ! directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

}
