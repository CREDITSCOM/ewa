import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Contract extends SmartContract {
    int counterTaskId;
    String smartOwner;
    User smartOwnerUser;
    private Map<String, Task> tasks;
    private Map<String, User> users;


    public Contract() {
        counterTaskId = 0;
        smartOwner = "5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe";
        tasks = new HashMap<>();
        users = new HashMap<>();
        smartOwnerUser = new User(smartOwner, 0, "URL");
        users.put(smartOwner,smartOwnerUser);
    }

    //Регистрирует пользователя в смартконтракте
    public void register(String userAddress, double balance, String descriptionURL) {
        users.put(userAddress, new User(userAddress, balance, descriptionURL));
    }

    //Добавить баланс
    public void addBalance(String address, int tokens) throws Exception {
        User currentUser = getUserFromUserList(address);
        currentUser.balance += tokens;
        //проверить ожидают ли задачи юзера пополнения баланса
        resolveTasksLacks(currentUser);
    }

    //проверить таски которым нехватило баланса
    private void resolveTasksLacks(User currentUser) {
        currentUser.creatorTasks.values().forEach((task) -> {
            if (task.lack) {
                startTask(task, task.date);
            }
            ;
        });
    }

    //Перевод таски в инпрогресс
    public void startTask(Task task, Date date) {
        final User owner = task.owner;
        if (owner.balance - owner.ascrowBalance >= task.cost) {
            owner.ascrowBalance += task.cost;
            task.lack = false;
            task.doer.doerTasks.putIfAbsent(task.taskId, task);
            task.state = State.IN_PROGRESS;
            task.date = date;
            //sendInfoToCrowFavors() // послать инфу в стороннюю систему о начале задачи
        } else {
            task.lack = true;
            System.out.println("Cannot start task");
            //sendInfoToCrowFavors() Оповещение платформы о нехватке баланса
        }
    }

    //Создание задачи
    public void createTask(String address, double taskCost, String descriptionURL) throws Exception {
        User currentUser = getUserFromUserList(address);
        int taskId = getNewTaskId();
        Task task = new Task(taskId, currentUser, taskCost, descriptionURL);
        tasks.put(String.valueOf(task.taskId), task);
        currentUser.creatorTasks.put(taskId,task);
    }

    //Добавить исполнителя задачи
    public void requestForDoingTask(String address, int taskId) throws Exception {
        User currentUser = getUserFromUserList(address);
        Task task = getTaskFromTasksList(taskId);
        task.requesters.add(currentUser);
    }

    //Принять заявку
    public void applyRequster(String address, int taskId, int requesterId, Date date) throws Exception {
        Task task = getTaskFromTasksList(taskId);
        User doer = task.requesters.get(requesterId);
        task.doer = doer;
        startTask(task,date); //todo возможно стоит добавить подтверждение от исполнителя перед стартом
    }

    //Создать диспут
    public void createDispute(String address, int taskId, int newTaskCost) throws Exception {
        User currentUser = getUserFromUserList(address);
        Task task = getTaskFromTasksList(taskId);
        if (currentUser.id.equals(task.doer.id) || currentUser.id.equals(task.owner.id)) {
            Dispute dispute = new Dispute(newTaskCost);
            if (currentUser.id.equals(task.doer.id)) {
                dispute.applyDoer = true;
            }
            if (currentUser.id.equals(task.owner.id)) {
                dispute.applyOwner = true;
            }
            task.dispute = dispute;
        }
    }

    //Подтвердить диспут
    public void applyDisputeTask(String address, int taskId) throws Exception {
        User currentUser = getUserFromUserList(address);
        Task task = getTaskFromTasksList(taskId);
        if (currentUser.id.equals(task.doer.id) || currentUser.id.equals(task.owner.id)) {
            Dispute dispute = task.dispute;
            if (dispute != null) {
                if (dispute.applyOwner || dispute.applyDoer) {
                    if ((currentUser.id.equals(task.doer.id) && !dispute.applyDoer) ||
                        (currentUser.id.equals(task.owner.id) && !dispute.applyOwner)) {

                        applyDisputeTask(taskId);

                    } else {
                        throw new Exception("You have already given your apply");
                    }
                } else {
                    throw new Exception("Unable to apply this dispute");
                }
            } else {
                throw new Exception("Dispute is not found");
            }
        } else {
            throw new Exception("You don't work with this task");
        }

    }

    //Действия для применения диспута
    private void applyDisputeTask(int taskId) throws Exception {
        Task task = getTaskFromTasksList(taskId);
        int disputeTaskCost = task.dispute.disputeTaskCost;
        if (task.state.equals(State.IN_PROGRESS)) {
            task.state = State.DISPUTE;
            User owner = task.owner;
            if (owner.balance - owner.ascrowBalance + task.cost > disputeTaskCost) {
                owner.ascrowBalance -= task.cost;
                owner.ascrowBalance += task.cost;
                task.cost = disputeTaskCost;
                task.state = State.IN_PROGRESS;
            } else {
                task.state = State.IN_PROGRESS;
                throw new Exception("Cannot apply dispute, owner have low balance");
            }
        } else {
            throw new Exception("Task is not in progress");
        }
    }

    //Отмена задачи
    public void cancelTask(String address, int taskId) throws Exception {
        User currentUser = getUserFromUserList(address);
        Task task = getTaskFromTasksList(taskId);
        if (task.state.equals(State.IN_PROGRESS) &&
            (currentUser.id.equals(task.doer.id) || currentUser.id.equals(task.owner.id))) {
            applyCancelTask(taskId);
        } else {
            throw new Exception("You don't work with this task");
        }
    }

    //Действия для отмены
    private void applyCancelTask(int taskId) throws Exception {
        Task task = getTaskFromTasksList(taskId);
        task.owner.ascrowBalance -= task.cost;
        task.state = State.CANCEL;
    }

    //Завершение задачи
    public void finishTask(String address, int taskId) throws Exception {
        User currentUser = getUserFromUserList(address);
        Task task = getTaskFromTasksList(taskId);
        if (task.doer.id.equals(currentUser.id) && task.state.equals(State.IN_PROGRESS)) {
            finishTask(taskId);
        } else {
            throw new Exception("You is not doer this task");
        }
    }

    //Действия для завершение таска
    private void finishTask(int taskId) throws Exception {
        Task task = getTaskFromTasksList(taskId);
        task.state = State.FINISH;
        User owner = task.owner;
        owner.balance -= task.cost;
        owner.ascrowBalance -= task.cost;

        distribute(task.doer, task.owner, task.cost);

    }

    //распределить деньги
    private void distribute(User taskDoer, User taskOwner, double cost) throws Exception {

        double doerCost = cost * 0.85;
        double smartOwnerCommision = cost * 0.09;

        taskDoer.balance += doerCost;
        smartOwnerUser.balance += smartOwnerCommision;

        cost-=doerCost;
        cost-=smartOwnerCommision;


        double costDoerCoT = cost / 2;
        double costOwnerCoT = cost - costDoerCoT;
        cotDistribute(costDoerCoT, costDoerCoT/6, taskDoer,0);
        cotDistribute(costOwnerCoT, costOwnerCoT/6, taskOwner,0);
    }

    //Устновить рейтинг за выполненную задачу
    public void setRatingForTask(String address, int taskId, int rating) throws Exception {
        if(rating>5 || rating<1) {
            return;
        } else {
            User currentUser = getUserFromUserList(address);
            Task task = getTaskFromTasksList(taskId);
            if (task.state.equals(State.FINISH)) {
                if (currentUser.id.equals(task.doer.id)) {
                    task.owner.rating.add(rating);
                }
                if (currentUser.id.equals(task.owner.id)) {
                    task.doer.rating.add(rating);
                }
            }
        }
    }

    //получить новую таску
    private int getNewTaskId() {
        int taskId = counterTaskId;
        counterTaskId += 1;
        return taskId;
    }

    //Найти пользователя
    private User getUserFromUserList(String address) throws Exception {
        User user = users.get(address);
        if (user == null) {
            throw new Exception("User not found");
        }
        return user;
    }

    //Найти задачу
    private Task getTaskFromTasksList(int taskId) throws Exception {
        Task task = tasks.get(String.valueOf(taskId));
        if (task == null) {
            throw new Exception("Task is not found");
        }
        return task;
    }

    //распределение остатков по дереву CoT
    public void cotDistribute(double cost, double costToOneLevel, User user, int level) {
        if(user.parent!=null && checkCoTRules(user.parent) && level != 6) {
            User parent = user.parent;
            if(cost-costToOneLevel>0) {
                parent.balance += costToOneLevel;
                cost -= costToOneLevel;
                cotDistribute(cost,costToOneLevel,parent,level+1);
            } else {
                parent.balance += cost;
            }
        } else {
            smartOwnerUser.balance += cost;
        }
    }

    //Проверка правил CoT если средний рейтинг пользователя > 4 и количество тасок за последние 6 месяцев > 4
    private boolean checkCoTRules(User user) {
        double averageRating = 0;
        for (Integer rating:user.rating) {
            averageRating = averageRating + rating;
        }
        averageRating = averageRating/user.rating.size();

        int tasksForSixMonths=0;

        for (Task task : user.creatorTasks.values()) {
            if (getTasksForLastSixMonths(task)) {
                tasksForSixMonths++;
            }
        }

        for (Task task : user.doerTasks.values()) {
            if (getTasksForLastSixMonths(task)) {
                tasksForSixMonths++;
            }
        }

        return averageRating >= 4 && tasksForSixMonths >= 4;

    }

    //Проверка была ли выполнена таска за последние 6 месяцев
    private boolean getTasksForLastSixMonths(Task task) {
        Date today = new Date();
        return task.date.after(subtractSixMonth(today));
    }

    //Добавить себе родителя
    public void addUserToCoT(String myAddress, String parentAddress) throws Exception {
        User user = users.get(myAddress);
        try {
            user.parent = users.get(parentAddress);
        } catch (Exception e) {
            throw new Exception("User not found");
        }
    }

    //Удалить родителя, но зачем ?
    public void delUserFromCoT(String myAddress, String parentAddress) {
        User user = users.get(myAddress);
        user.parent = null;
    }

    public double getUserBalance(String address) {
        User user = users.get(address);
        return user.balance;
    }

    public enum State implements Serializable {
        NEW, CANCEL, DISPUTE, IN_PROGRESS, FINISH
    }


    public static class Task implements Serializable {
        int taskId;
        User owner;
        User doer;
        Dispute dispute;
        List<User> requesters;
        String descriptionURL;
        double cost;
        boolean lack;
        State state;
        Date date;

        Task(int taskId, User owner, double cost, String descriptionURL) {
            this.taskId = taskId;
            this.owner = owner;
            this.descriptionURL = descriptionURL;
            this.cost = cost;
            this.date = new Date(); //todo установить только текущий день, а не timestamp
            this.state = State.NEW;
            this.requesters = new ArrayList<>();
        }
    }

    public static class User implements Serializable {
        String id;
        double balance;
        double ascrowBalance;
        User parent;
        String descriptionURL;
        Map<Integer,Task> creatorTasks;
        Map<Integer,Task> doerTasks;
        List<Integer> rating;

        User(String address, double balance, String descriptionURL) {
            this.id = address;
            this.balance = balance;
            this.descriptionURL = descriptionURL;
            this.rating = new ArrayList<>();
            this.creatorTasks = new HashMap<>();
            this.doerTasks = new HashMap<>();
        }
    }

    private static class Dispute implements Serializable{
        int disputeTaskCost;
        boolean applyOwner;
        boolean applyDoer;

        Dispute(int disputeTaskCost) {
            this.disputeTaskCost = disputeTaskCost;
        }
    }

    private static Date subtractSixMonth(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -6); //minus number would decrement the days
        return cal.getTime();
    }

}
