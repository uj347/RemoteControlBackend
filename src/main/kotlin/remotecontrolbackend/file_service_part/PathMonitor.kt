package remotecontrolbackend.file_service_part

import remotecontrolbackend.file_service_part.path_repo_part.IFilePathRepo
import kotlin.coroutines.CoroutineContext
//Монитор на корутинах, используя коммонс ио файл чэнж листенер
//Подумать стоит ли заморачиваться с инжектом или же просто создать инстанс в файл сервисе
// Как вариант  - прсто убрать из конструктора файл репо , добавить метод сетРепо и нит, чтобы дать стартовые параметры,а дальше пусть ебется сам
class PathMonitor(repo: IFilePathRepo,fileServiceContext:CoroutineContext) {
}